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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupExportStats(
  val cardCount: Int,
  val categoryCount: Int,
  val photoCount: Int
)

data class BackupRestorePayload(
  val categories: List<CardCategory>,
  val cards: List<PhysicalCard>,
  val restoredPhotoCount: Int
)

object VaultBackupManager {

  fun defaultBackupFileName(): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    return "SageVault_Backup_$dateStr.zip"
  }

  suspend fun exportBackupToUri(
    context: Context,
    targetUri: Uri,
    categories: List<CardCategory>,
    cards: List<PhysicalCard>
  ): Result<BackupExportStats> {
    return withContext(Dispatchers.IO) {
      try {
        val stats = context.contentResolver.openOutputStream(targetUri)?.use { out ->
          writeBackupZipStream(out, categories, cards)
        } ?: return@withContext Result.failure(IllegalStateException("Unable to open destination file."))
        Result.success(stats)
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
        val tempDir = File(context.cacheDir, "temp_images").apply { mkdirs() }
        val backupFile = File(tempDir, defaultBackupFileName())
        FileOutputStream(backupFile).use { out ->
          writeBackupZipStream(out, categories, cards)
        }
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          backupFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "application/zip"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_SUBJECT, "SageVault Complete Backup (Cards + Photos)")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Result.success(Intent.createChooser(shareIntent, "Share SageVault Backup (.zip)"))
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
        val rawBytes = context.contentResolver.openInputStream(sourceUri)?.use { input ->
          input.readBytes()
        } ?: return@withContext Result.failure(IllegalStateException("Unable to read backup file."))

        if (rawBytes.isEmpty()) {
          return@withContext Result.failure(IllegalStateException("Backup file is empty."))
        }

        // Check if file is a ZIP archive (PK\x03\x04 magic header) or legacy JSON
        val isZip = rawBytes.size >= 4 &&
          rawBytes[0] == 0x50.toByte() &&
          rawBytes[1] == 0x4B.toByte()

        var jsonText: String? = null
        val zipPhotos = mutableMapOf<String, ByteArray>()

        if (isZip) {
          ZipInputStream(ByteArrayInputStream(rawBytes)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
              val entryName = entry.name
              if (!entry.isDirectory) {
                val entryBytes = zis.readBytes()
                if (entryName == "vault_data.json" || entryName.endsWith(".json")) {
                  jsonText = entryBytes.toString(Charsets.UTF_8)
                } else {
                  zipPhotos[entryName] = entryBytes
                }
              }
              zis.closeEntry()
              entry = zis.nextEntry
            }
          }
        } else {
          jsonText = rawBytes.toString(Charsets.UTF_8)
        }

        val finalJson = jsonText
          ?: return@withContext Result.failure(IllegalStateException("vault_data.json not found in backup."))

        val root = JSONObject(finalJson)
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
        var restoredPhotoCount = 0

        for (i in 0 until cardsArray.length()) {
          val obj = cardsArray.optJSONObject(i) ?: continue
          val title = obj.optString("title", "").trim()
          if (title.isEmpty()) continue

          val frontZipEntry = obj.optString("frontZipEntry", "")
          val backZipEntry = obj.optString("backZipEntry", "")
          val frontBase64 = obj.optString("frontImageBase64", "")
          val backBase64 = obj.optString("backImageBase64", "")

          val restoredFrontPath = when {
            frontZipEntry.isNotBlank() && zipPhotos.containsKey(frontZipEntry) -> {
              savePhotoBytesToInternalStorage(context, zipPhotos.getValue(frontZipEntry), "front")
            }
            frontBase64.isNotBlank() -> {
              decodeBase64ToImageFile(context, frontBase64, "front")
            }
            else -> null
          }
          if (restoredFrontPath != null) restoredPhotoCount++

          val restoredBackPath = when {
            backZipEntry.isNotBlank() && zipPhotos.containsKey(backZipEntry) -> {
              savePhotoBytesToInternalStorage(context, zipPhotos.getValue(backZipEntry), "back")
            }
            backBase64.isNotBlank() -> {
              decodeBase64ToImageFile(context, backBase64, "back")
            }
            else -> null
          }
          if (restoredBackPath != null) restoredPhotoCount++

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
            cards = parsedCards,
            restoredPhotoCount = restoredPhotoCount
          )
        )
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  private suspend fun writeBackupZipStream(
    outputStream: OutputStream,
    categories: List<CardCategory>,
    cards: List<PhysicalCard>
  ): BackupExportStats {
    var exportedPhotoCount = 0

    ZipOutputStream(outputStream).use { zos ->
      val root = JSONObject()
      root.put("app", "SageVault")
      root.put("backupVersion", 2)
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
      cards.forEachIndexed { index, card ->
        val frontBytes = readPhotoBytes(card.frontImagePath)
        val backBytes = readPhotoBytes(card.backImagePath)

        var frontEntryName = ""
        if (frontBytes != null && frontBytes.isNotEmpty()) {
          frontEntryName = "photos/card_${index + 1}_front.jpg"
          zos.putNextEntry(ZipEntry(frontEntryName))
          zos.write(frontBytes)
          zos.closeEntry()
          exportedPhotoCount++
        }

        var backEntryName = ""
        if (backBytes != null && backBytes.isNotEmpty()) {
          backEntryName = "photos/card_${index + 1}_back.jpg"
          zos.putNextEntry(ZipEntry(backEntryName))
          zos.write(backBytes)
          zos.closeEntry()
          exportedPhotoCount++
        }

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
          put("frontZipEntry", frontEntryName)
          put("backZipEntry", backEntryName)
          put(
            "frontImageBase64",
            if (frontBytes != null) Base64.encodeToString(frontBytes, Base64.NO_WRAP) else ""
          )
          put(
            "backImageBase64",
            if (backBytes != null) Base64.encodeToString(backBytes, Base64.NO_WRAP) else ""
          )
        }
        cardsArray.put(cardObj)
      }
      root.put("cards", cardsArray)

      val jsonBytes = root.toString(2).toByteArray(Charsets.UTF_8)
      zos.putNextEntry(ZipEntry("vault_data.json"))
      zos.write(jsonBytes)
      zos.closeEntry()
      zos.finish()
    }

    return BackupExportStats(
      cardCount = cards.size,
      categoryCount = categories.size,
      photoCount = exportedPhotoCount
    )
  }

  private suspend fun readPhotoBytes(filePath: String?): ByteArray? {
    if (filePath.isNullOrBlank()) return null
    val file = File(filePath)
    if (!file.exists() || file.length() == 0L) return null
    return try {
      if (file.length() <= 1_500_000L) {
        file.readBytes()
      } else {
        val bmp = ImageStorageHelper.loadBitmapFromFile(filePath, maxDimension = 1400)
        if (bmp != null) {
          ByteArrayOutputStream().use { bos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 88, bos)
            bos.toByteArray()
          }
        } else {
          file.readBytes()
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun savePhotoBytesToInternalStorage(
    context: Context,
    bytes: ByteArray,
    prefix: String
  ): String? {
    if (bytes.isEmpty()) return null
    return try {
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

  private fun decodeBase64ToImageFile(
    context: Context,
    base64Data: String,
    prefix: String
  ): String? {
    return try {
      val bytes = Base64.decode(base64Data, Base64.DEFAULT)
      savePhotoBytesToInternalStorage(context, bytes, prefix)
    } catch (_: Exception) {
      null
    }
  }
}
