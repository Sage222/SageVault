package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.zxing.BarcodeFormat

enum class BarcodeFormatType(
  val displayName: String,
  val is2D: Boolean,
  val zxingFormat: BarcodeFormat?
) {
  QR_CODE("QR Code", true, BarcodeFormat.QR_CODE),
  CODE_128("Code 128 (Barcode)", false, BarcodeFormat.CODE_128),
  CODE_39("Code 39 (Library/Gym)", false, BarcodeFormat.CODE_39),
  EAN_13("EAN-13 (Retail)", false, BarcodeFormat.EAN_13),
  UPC_A("UPC-A", false, BarcodeFormat.UPC_A),
  PDF_417("PDF417 (ID/Insurance)", true, BarcodeFormat.PDF_417),
  AZTEC("Aztec Code", true, BarcodeFormat.AZTEC),
  CODABAR("Codabar (Library)", false, BarcodeFormat.CODABAR),
  NONE("No Barcode (Photo / ID Only)", false, null);

  companion object {
    fun fromName(name: String): BarcodeFormatType {
      return entries.find { it.name == name } ?: CODE_128
    }

    fun fromZxingFormat(format: BarcodeFormat): BarcodeFormatType {
      return when (format) {
        BarcodeFormat.QR_CODE -> QR_CODE
        BarcodeFormat.CODE_128 -> CODE_128
        BarcodeFormat.CODE_39, BarcodeFormat.CODE_93 -> CODE_39
        BarcodeFormat.EAN_13, BarcodeFormat.EAN_8 -> EAN_13
        BarcodeFormat.UPC_A, BarcodeFormat.UPC_E -> UPC_A
        BarcodeFormat.PDF_417 -> PDF_417
        BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX -> AZTEC
        BarcodeFormat.CODABAR -> CODABAR
        else -> CODE_128
      }
    }
  }
}

@Entity(tableName = "categories")
data class CardCategory(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val name: String,
  val iconName: String,
  val colorHex: Long,
  val sortOrder: Int = 0,
  val isDefault: Boolean = false
)

@Entity(tableName = "cards")
data class PhysicalCard(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val subtitle: String = "",
  val cardNumber: String = "",
  val barcodeValue: String = "",
  val barcodeFormat: String = BarcodeFormatType.CODE_128.name,
  val categoryId: Long = 1L,
  val colorHex: Long = 0xFF10B981L,
  val frontImagePath: String? = null,
  val backImagePath: String? = null,
  val notes: String = "",
  val isFavorite: Boolean = false,
  val favoriteOrder: Int = 0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
