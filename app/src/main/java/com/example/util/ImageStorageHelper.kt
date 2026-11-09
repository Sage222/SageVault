package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import com.example.data.BarcodeFormatType
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocalCardImage(
  filePath: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop
) {
  val imageBitmap = produceState<ImageBitmap?>(initialValue = null, key1 = filePath) {
    value = withContext(Dispatchers.IO) {
      try {
        val file = File(filePath)
        if (!file.exists()) return@withContext null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        var sampleSize = 1
        while (options.outWidth / sampleSize > 1280 || options.outHeight / sampleSize > 1280) {
          sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeFile(file.absolutePath, decodeOptions)?.asImageBitmap()
      } catch (_: Exception) {
        null
      }
    }
  }
  val bmp = imageBitmap.value
  if (bmp != null) {
    Image(
      bitmap = bmp,
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = modifier
    )
  } else {
    Box(modifier = modifier)
  }
}

data class ScannedBarcodeResult(
  val rawValue: String,
  val formatType: BarcodeFormatType
)

object ImageStorageHelper {

  private val readerHints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
    put(
      DecodeHintType.POSSIBLE_FORMATS,
      listOf(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.PDF_417,
        BarcodeFormat.AZTEC,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.CODABAR,
        BarcodeFormat.ITF
      )
    )
    put(DecodeHintType.TRY_HARDER, true)
  }

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

  fun decodeFromCameraFrame(imageProxy: ImageProxy): ScannedBarcodeResult? {
    return try {
      val plane = imageProxy.planes.firstOrNull() ?: return null
      val buffer = plane.buffer
      val data = ByteArray(buffer.remaining())
      buffer.get(data)
      val width = imageProxy.width
      val height = imageProxy.height

      val reader = MultiFormatReader().apply { setHints(readerHints) }

      // 1. Try standard orientation
      decodeYuvBytes(reader, data, width, height)?.let { return it }

      // 2. Try 90-degree rotated orientation (for vertical 1D barcodes in portrait mode)
      val rotated = ByteArray(data.size)
      for (y in 0 until height) {
        for (x in 0 until width) {
          val destIdx = x * height + (height - y - 1)
          val srcIdx = y * width + x
          if (destIdx in rotated.indices && srcIdx in data.indices) {
            rotated[destIdx] = data[srcIdx]
          }
        }
      }
      decodeYuvBytes(reader, rotated, height, width)
    } catch (_: Exception) {
      null
    }
  }

  private fun decodeYuvBytes(
    reader: MultiFormatReader,
    yuvData: ByteArray,
    width: Int,
    height: Int
  ): ScannedBarcodeResult? {
    val source = PlanarYUVLuminanceSource(yuvData, width, height, 0, 0, width, height, false)
    return try {
      val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
      val text = result.text?.trim().orEmpty()
      if (text.isNotEmpty()) {
        ScannedBarcodeResult(text, BarcodeFormatType.fromZxingFormat(result.barcodeFormat))
      } else null
    } catch (_: Exception) {
      try {
        val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
        val text = result.text?.trim().orEmpty()
        if (text.isNotEmpty()) {
          ScannedBarcodeResult(text, BarcodeFormatType.fromZxingFormat(result.barcodeFormat))
        } else null
      } catch (_: Exception) {
        null
      }
    } finally {
      reader.reset()
    }
  }

  suspend fun scanBarcodeFromImageUri(context: Context, uri: Uri): ScannedBarcodeResult? {
    return withContext(Dispatchers.IO) {
      try {
        val bitmap: Bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)
        } ?: return@withContext null

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val reader = MultiFormatReader().apply { setHints(readerHints) }
        try {
          val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
          val text = result.text?.trim().orEmpty()
          if (text.isNotEmpty()) {
            ScannedBarcodeResult(text, BarcodeFormatType.fromZxingFormat(result.barcodeFormat))
          } else null
        } catch (_: Exception) {
          val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
          val text = result.text?.trim().orEmpty()
          if (text.isNotEmpty()) {
            ScannedBarcodeResult(text, BarcodeFormatType.fromZxingFormat(result.barcodeFormat))
          } else null
        }
      } catch (_: Exception) {
        null
      }
    }
  }
}
