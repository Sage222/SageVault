package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
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
    value = ImageStorageHelper.loadBitmapFromFile(filePath, maxDimension = 1280)?.asImageBitmap()
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

  suspend fun loadBitmapFromFile(filePath: String, maxDimension: Int = 1600): Bitmap? {
    return withContext(Dispatchers.IO) {
      try {
        val file = File(filePath)
        if (!file.exists()) return@withContext null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
          sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withContext null

        val exifRotation = try {
          val exif = ExifInterface(file.absolutePath)
          when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
          }
        } catch (_: Exception) {
          0f
        }

        if (exifRotation != 0f) {
          rotateBitmap(rawBitmap, exifRotation)
        } else {
          rawBitmap
        }
      } catch (_: Exception) {
        null
      }
    }
  }

  fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    if (degrees % 360f == 0f) return source
    val matrix = Matrix().apply { postRotate(degrees) }
    return try {
      Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    } catch (_: Exception) {
      source
    }
  }

  suspend fun saveCroppedBitmap(
    context: Context,
    sourceBitmap: Bitmap,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    prefix: String = "card"
  ): String? {
    return withContext(Dispatchers.IO) {
      try {
        val bmpW = sourceBitmap.width
        val bmpH = sourceBitmap.height
        if (bmpW <= 0 || bmpH <= 0) return@withContext null

        val leftPx = (cropLeft.coerceIn(0f, 0.95f) * bmpW).roundToInt().coerceIn(0, max(0, bmpW - 1))
        val topPx = (cropTop.coerceIn(0f, 0.95f) * bmpH).roundToInt().coerceIn(0, max(0, bmpH - 1))
        val rightPx = (cropRight.coerceIn(0.05f, 1f) * bmpW).roundToInt().coerceIn(leftPx + 1, bmpW)
        val bottomPx = (cropBottom.coerceIn(0.05f, 1f) * bmpH).roundToInt().coerceIn(topPx + 1, bmpH)

        val widthPx = max(1, min(bmpW - leftPx, rightPx - leftPx))
        val heightPx = max(1, min(bmpH - topPx, bottomPx - topPx))

        val cropped = Bitmap.createBitmap(sourceBitmap, leftPx, topPx, widthPx, heightPx)
        val dir = File(context.filesDir, "card_images").apply { mkdirs() }
        val destFile = File(dir, "${prefix}_crop_${UUID.randomUUID()}.jpg")
        FileOutputStream(destFile).use { out ->
          cropped.compress(Bitmap.CompressFormat.JPEG, 92, out)
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
