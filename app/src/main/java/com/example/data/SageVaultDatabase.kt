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
  version = 2,
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
      }
    }
  }
}
