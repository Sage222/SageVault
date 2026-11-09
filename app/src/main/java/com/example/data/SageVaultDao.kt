package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SageVaultDao {

  // --- Categories ---
  @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
  fun observeCategories(): Flow<List<CardCategory>>

  @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
  suspend fun getAllCategoriesOnce(): List<CardCategory>

  @Query("SELECT COUNT(*) FROM categories")
  suspend fun getCategoryCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CardCategory): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CardCategory>)

  @Update
  suspend fun updateCategory(category: CardCategory)

  @Delete
  suspend fun deleteCategory(category: CardCategory)

  @Query("UPDATE cards SET categoryId = :fallbackCategoryId WHERE categoryId = :deletedCategoryId")
  suspend fun reassignCardsToCategory(deletedCategoryId: Long, fallbackCategoryId: Long)

  // --- Physical Cards ---
  @Query("SELECT * FROM cards ORDER BY isFavorite DESC, favoriteOrder ASC, updatedAt DESC")
  fun observeAllCards(): Flow<List<PhysicalCard>>

  @Query("SELECT * FROM cards ORDER BY isFavorite DESC, favoriteOrder ASC, updatedAt DESC")
  suspend fun getAllCardsOnce(): List<PhysicalCard>

  @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY favoriteOrder ASC, updatedAt DESC")
  fun observeFavoriteCards(): Flow<List<PhysicalCard>>

  @Query("SELECT COUNT(*) FROM cards")
  suspend fun getCardCount(): Int

  @Query("SELECT COALESCE(MAX(favoriteOrder), 0) FROM cards WHERE isFavorite = 1")
  suspend fun getMaxFavoriteOrder(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCard(card: PhysicalCard): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCards(cards: List<PhysicalCard>)

  @Update
  suspend fun updateCard(card: PhysicalCard)

  @Delete
  suspend fun deleteCard(card: PhysicalCard)

  @Query("UPDATE cards SET title = :newTitle, updatedAt = :updatedAt WHERE id = :cardId")
  suspend fun renameCard(cardId: Long, newTitle: String, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE cards SET isFavorite = :isFavorite, favoriteOrder = :favoriteOrder, updatedAt = :updatedAt WHERE id = :cardId")
  suspend fun setCardFavorite(
    cardId: Long,
    isFavorite: Boolean,
    favoriteOrder: Int,
    updatedAt: Long = System.currentTimeMillis()
  )

  @Query("UPDATE cards SET favoriteOrder = :order WHERE id = :cardId")
  suspend fun updateCardFavoriteOrder(cardId: Long, order: Int)

  @Transaction
  suspend fun updateFavoritesOrder(orderedCardIds: List<Long>) {
    orderedCardIds.forEachIndexed { index, cardId ->
      updateCardFavoriteOrder(cardId, index)
    }
  }
}
