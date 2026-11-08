package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.BarcodeFormatType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.EnumMap

object BarcodeGenerator {

  fun generateBarcodeBitmap(
    content: String,
    formatType: BarcodeFormatType,
    width: Int = 800,
    height: Int = if (formatType.is2D) 800 else 320
  ): ImageBitmap? {
    val cleanContent = content.trim()
    if (cleanContent.isEmpty() || formatType == BarcodeFormatType.NONE) return null
    val zxingFormat = formatType.zxingFormat ?: BarcodeFormat.CODE_128

    return try {
      encodeToImageBitmap(cleanContent, zxingFormat, width, height)
    } catch (e: Exception) {
      // Fallback to CODE_128 or QR_CODE if strict format checksum (e.g. EAN_13 / UPC_A) fails
      try {
        val fallbackFormat = if (formatType.is2D) BarcodeFormat.QR_CODE else BarcodeFormat.CODE_128
        encodeToImageBitmap(cleanContent, fallbackFormat, width, height)
      } catch (_: Exception) {
        try {
          encodeToImageBitmap(cleanContent, BarcodeFormat.QR_CODE, 600, 600)
        } catch (_: Exception) {
          null
        }
      }
    }
  }

  private fun encodeToImageBitmap(
    content: String,
    format: BarcodeFormat,
    width: Int,
    height: Int
  ): ImageBitmap {
    val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
      put(EncodeHintType.CHARACTER_SET, "UTF-8")
      put(EncodeHintType.MARGIN, if (format == BarcodeFormat.QR_CODE) 1 else 2)
    }
    val bitMatrix = MultiFormatWriter().encode(content, format, width, height, hints)
    val matrixWidth = bitMatrix.width
    val matrixHeight = bitMatrix.height
    val pixels = IntArray(matrixWidth * matrixHeight)

    for (y in 0 until matrixHeight) {
      val offset = y * matrixWidth
      for (x in 0 until matrixWidth) {
        pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
      }
    }

    val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
    return bitmap.asImageBitmap()
  }
}
