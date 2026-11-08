package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ui.theme.CardPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [PhysicalCard::class, CardCategory::class],
  version = 1,
  exportSchema = false
)
abstract class SageVaultDatabase : RoomDatabase() {
  abstract fun sageVaultDao(): SageVaultDao

  companion object {
    @Volatile
    private var INSTANCE: SageVaultDatabase? = null

    fun getDatabase(context: Context): SageVaultDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SageVaultDatabase::class.java,
          "sage_vault_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        CoroutineScope(Dispatchers.IO).launch {
          seedDefaultCategoriesIfNeeded(instance.sageVaultDao())
        }
        instance
      }
    }

    suspend fun seedDefaultCategoriesIfNeeded(dao: SageVaultDao) {
      if (dao.getCategoryCount() == 0) {
        val defaultCategories = listOf(
          CardCategory(
            id = 1L,
            name = "Library",
            iconName = "library",
            colorHex = CardPalette.LIBRARY_INDIGO,
            sortOrder = 0,
            isDefault = true
          ),
          CardCategory(
            id = 2L,
            name = "Gym & Fitness",
            iconName = "gym",
            colorHex = CardPalette.GYM_CRIMSON,
            sortOrder = 1,
            isDefault = true
          ),
          CardCategory(
            id = 3L,
            name = "Insurance",
            iconName = "insurance",
            colorHex = CardPalette.INSURANCE_CYAN,
            sortOrder = 2,
            isDefault = true
          ),
          CardCategory(
            id = 4L,
            name = "Medical & Dental",
            iconName = "medical",
            colorHex = CardPalette.MEDICAL_TEAL,
            sortOrder = 3,
            isDefault = false
          ),
          CardCategory(
            id = 5L,
            name = "Membership & Club",
            iconName = "membership",
            colorHex = CardPalette.SAGE_EMERALD,
            sortOrder = 4,
            isDefault = false
          ),
          CardCategory(
            id = 6L,
            name = "Retail & Rewards",
            iconName = "shopping",
            colorHex = CardPalette.RETAIL_AMBER,
            sortOrder = 5,
            isDefault = false
          )
        )
        dao.insertCategories(defaultCategories)

        // Seed 3 starter example cards (Library, Gym, Insurance) so the user can test
        // barcode rendering, renaming, and favorites reordering right away, or edit/delete them.
        if (dao.getCardCount() == 0) {
          val now = System.currentTimeMillis()
          dao.insertCards(
            listOf(
              PhysicalCard(
                title = "City Central Library",
                subtitle = "Public Library Patron Pass",
                cardNumber = "29041-88492-01",
                barcodeValue = "290418849201",
                barcodeFormat = BarcodeFormatType.CODE_128.name,
                categoryId = 1L,
                colorHex = CardPalette.LIBRARY_INDIGO,
                notes = "Main branch & digital audiobook access. Renews annually.",
                isFavorite = true,
                favoriteOrder = 0,
                createdAt = now - 300_000L,
                updatedAt = now - 100_000L
              ),
              PhysicalCard(
                title = "Apex Athletics Gym",
                subtitle = "24/7 Keytag Check-In",
                cardNumber = "GYM-88420-X",
                barcodeValue = "APEX-GYM-88420-X",
                barcodeFormat = BarcodeFormatType.QR_CODE.name,
                categoryId = 2L,
                colorHex = CardPalette.GYM_CRIMSON,
                notes = "Scan QR at front turnstile. Locker towel service included.",
                isFavorite = true,
                favoriteOrder = 1,
                createdAt = now - 200_000L,
                updatedAt = now - 50_000L
              ),
              PhysicalCard(
                title = "BlueShield Health Plan",
                subtitle = "PPO Medical & Pharmacy ID",
                cardNumber = "GRP-774109-M",
                barcodeValue = "BS-PPO-774109-M",
                barcodeFormat = BarcodeFormatType.PDF_417.name,
                categoryId = 3L,
                colorHex = CardPalette.INSURANCE_CYAN,
                notes = "RxBIN: 004336 | RxPCN: ADV | Primary Care Copay: $20",
                isFavorite = true,
                favoriteOrder = 2,
                createdAt = now - 100_000L,
                updatedAt = now
              )
            )
          )
        }
      }
    }
  }
}
