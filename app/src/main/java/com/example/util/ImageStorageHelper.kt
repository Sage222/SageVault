package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.BarcodeFormatType
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class ScannedBarcodeResult(
  val rawValue: String,
  val formatType: BarcodeFormatType
)

object ImageStorageHelper {

  fun createTempCameraUri(context: Context): Pair<Uri, File> {
    val tempDir = File(context.cacheDir, "temp_images").apply { mkdirs() }
    val tempFile = File(tempDir, "capture_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      tempFile
    )
    return uri to tempFile
  }

  suspend fun copyUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "card"): String? {
    return withContext(Dispatchers.IO) {
      try {
        val dir = File(context.filesDir, "card_images").apply { mkdirs() }
        val destFile = File(dir, "${prefix}_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
          FileOutputStream(destFile).use { output ->
            input.copyTo(output)
          }
        }
        if (destFile.exists() && destFile.length() > 0L) {
          destFile.absolutePath
        } else {
          null
        }
      } catch (e: Exception) {
        null
      }
    }
  }

  suspend fun persistTempFile(context: Context, tempFile: File, prefix: String = "card"): String? {
    return withContext(Dispatchers.IO) {
      try {
        if (!tempFile.exists() || tempFile.length() == 0L) return@withContext null
        val dir = File(context.filesDir, "card_images").apply { mkdirs() }
        val destFile = File(dir, "${prefix}_${UUID.randomUUID()}.jpg")
        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete()
        destFile.absolutePath
      } catch (e: Exception) {
        null
      }
    }
  }

  suspend fun scanBarcodeFromImageUri(context: Context, uri: Uri): ScannedBarcodeResult? {
    return try {
      val image = withContext(Dispatchers.IO) {
        InputImage.fromFilePath(context, uri)
      }
      suspendCancellableCoroutine { continuation ->
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
          .addOnSuccessListener { barcodes ->
            val firstValid = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
            if (firstValid != null && !firstValid.rawValue.isNullOrBlank()) {
              continuation.resume(
                ScannedBarcodeResult(
                  rawValue = firstValid.rawValue!!.trim(),
                  formatType = BarcodeFormatType.fromMlKitFormat(firstValid.format)
                )
              )
            } else {
              continuation.resume(null)
            }
          }
          .addOnFailureListener {
            continuation.resume(null)
          }
      }
    } catch (e: Exception) {
      null
    }
  }
}
