package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.data.BarcodeFormatType
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupRestorePayload(
  val categories: List<CardCategory>,
  val cards: List<PhysicalCard>
)

object VaultBackupManager {

  fun defaultBackupFileName(): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    return "SageVault_Backup_$dateStr.json"
  }

  suspend fun exportBackupToUri(
    context: Context,
    targetUri: Uri,
    categories: List<CardCategory>,
    cards: List<PhysicalCard>
  ): Result<Int> {
    return withContext(Dispatchers.IO) {
      try {
        val jsonString = buildBackupJsonString(categories, cards)
        context.contentResolver.openOutputStream(targetUri)?.use { out ->
          out.write(jsonString.toByteArray(Charsets.UTF_8))
          out.flush()
        } ?: return@withContext Result.failure(IllegalStateException("Unable to open destination file."))
        Result.success(cards.size)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  suspend fun createShareableBackupIntent(
    context: Context,
    categories: List<CardCategory>,
    cards: List<PhysicalCard>
  ): Result<Intent> {
    return withContext(Dispatchers.IO) {
      try {
        val jsonString = buildBackupJsonString(categories, cards)
        val tempDir = File(context.cacheDir, "temp_images").apply { mkdirs() }
        val backupFile = File(tempDir, defaultBackupFileName())
        FileOutputStream(backupFile).use { out ->
          out.write(jsonString.toByteArray(Charsets.UTF_8))
          out.flush()
        }
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          backupFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "application/json"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_SUBJECT, "SageVault Cards Backup")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Result.success(Intent.createChooser(shareIntent, "Share SageVault Backup"))
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  suspend fun parseBackupFromUri(
    context: Context,
    sourceUri: Uri
  ): Result<BackupRestorePayload> {
    return withContext(Dispatchers.IO) {
      try {
        val rawJson = context.contentResolver.openInputStream(sourceUri)?.use { input ->
          input.bufferedReader(Charsets.UTF_8).readText()
        } ?: return@withContext Result.failure(IllegalStateException("Unable to read backup file."))

        val root = JSONObject(rawJson)
        val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
        val cardsArray = root.optJSONArray("cards") ?: JSONArray()

        val parsedCategories = mutableListOf<CardCategory>()
        for (i in 0 until categoriesArray.length()) {
          val obj = categoriesArray.optJSONObject(i) ?: continue
          val name = obj.optString("name", "").trim()
          if (name.isEmpty()) continue
          parsedCategories.add(
            CardCategory(
              id = obj.optLong("id", (i + 1).toLong()),
              name = name,
              iconName = obj.optString("iconName", "card"),
              colorHex = obj.optLong("colorHex", 0xFF10B981L),
              sortOrder = obj.optInt("sortOrder", i),
              isDefault = obj.optBoolean("isDefault", false)
            )
          )
        }

        val parsedCards = mutableListOf<PhysicalCard>()
        for (i in 0 until cardsArray.length()) {
          val obj = cardsArray.optJSONObject(i) ?: continue
          val title = obj.optString("title", "").trim()
          if (title.isEmpty()) continue

          val frontBase64 = obj.optString("frontImageBase64", "")
          val backBase64 = obj.optString("backImageBase64", "")

          val restoredFrontPath = if (frontBase64.isNotBlank()) {
            decodeBase64ToImageFile(context, frontBase64, "front")
          } else {
            null
          }

          val restoredBackPath = if (backBase64.isNotBlank()) {
            decodeBase64ToImageFile(context, backBase64, "back")
          } else {
            null
          }

          parsedCards.add(
            PhysicalCard(
              id = 0L,
              title = title,
              subtitle = obj.optString("subtitle", ""),
              cardNumber = obj.optString("cardNumber", ""),
              barcodeValue = obj.optString("barcodeValue", ""),
              barcodeFormat = obj.optString("barcodeFormat", BarcodeFormatType.CODE_128.name),
              categoryId = obj.optLong("categoryId", 1L),
              colorHex = obj.optLong("colorHex", 0xFF10B981L),
              frontImagePath = restoredFrontPath,
              backImagePath = restoredBackPath,
              notes = obj.optString("notes", ""),
              isFavorite = obj.optBoolean("isFavorite", false),
              favoriteOrder = obj.optInt("favoriteOrder", i),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
          )
        }

        Result.success(
          BackupRestorePayload(
            categories = parsedCategories,
            cards = parsedCards
          )
        )
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  private suspend fun buildBackupJsonString(
    categories: List<CardCategory>,
    cards: List<PhysicalCard>
  ): String {
    val root = JSONObject()
    root.put("app", "SageVault")
    root.put("backupVersion", 1)
    root.put("exportedAt", System.currentTimeMillis())

    val categoriesArray = JSONArray()
    categories.forEach { cat ->
      val catObj = JSONObject().apply {
        put("id", cat.id)
        put("name", cat.name)
        put("iconName", cat.iconName)
        put("colorHex", cat.colorHex)
        put("sortOrder", cat.sortOrder)
        put("isDefault", cat.isDefault)
      }
      categoriesArray.put(catObj)
    }
    root.put("categories", categoriesArray)

    val cardsArray = JSONArray()
    cards.forEach { card ->
      val cardObj = JSONObject().apply {
        put("id", card.id)
        put("title", card.title)
        put("subtitle", card.subtitle)
        put("cardNumber", card.cardNumber)
        put("barcodeValue", card.barcodeValue)
        put("barcodeFormat", card.barcodeFormat)
        put("categoryId", card.categoryId)
        put("colorHex", card.colorHex)
        put("notes", card.notes)
        put("isFavorite", card.isFavorite)
        put("favoriteOrder", card.favoriteOrder)
        put("createdAt", card.createdAt)
        put("updatedAt", card.updatedAt)
        put("frontImageBase64", encodeImageFileToBase64(card.frontImagePath) ?: "")
        put("backImageBase64", encodeImageFileToBase64(card.backImagePath) ?: "")
      }
      cardsArray.put(cardObj)
    }
    root.put("cards", cardsArray)

    return root.toString(2)
  }

  private suspend fun encodeImageFileToBase64(filePath: String?): String? {
    if (filePath.isNullOrBlank()) return null
    val file = File(filePath)
    if (!file.exists() || file.length() == 0L) return null
    return try {
      val bytes = if (file.length() <= 900_000L) {
        file.readBytes()
      } else {
        val bmp = ImageStorageHelper.loadBitmapFromFile(filePath, maxDimension = 1280)
        if (bmp != null) {
          ByteArrayOutputStream().use { bos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, bos)
            bos.toByteArray()
          }
        } else {
          file.readBytes()
        }
      }
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (_: Exception) {
      null
    }
  }

  private fun decodeBase64ToImageFile(
    context: Context,
    base64Data: String,
    prefix: String
  ): String? {
    return try {
      val bytes = Base64.decode(base64Data, Base64.DEFAULT)
      if (bytes.isEmpty()) return null
      val dir = File(context.filesDir, "card_images").apply { mkdirs() }
      val destFile = File(dir, "${prefix}_restored_${UUID.randomUUID()}.jpg")
      FileOutputStream(destFile).use { out ->
        out.write(bytes)
        out.flush()
      }
      if (destFile.exists() && destFile.length() > 0L) {
        destFile.absolutePath
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }
}
