package com.example.data

import kotlinx.coroutines.flow.Flow

class SageVaultRepository(private val dao: SageVaultDao) {

  val categories: Flow<List<CardCategory>> = dao.observeCategories()
  val allCards: Flow<List<PhysicalCard>> = dao.observeAllCards()
  val favoriteCards: Flow<List<PhysicalCard>> = dao.observeFavoriteCards()

  suspend fun saveCard(card: PhysicalCard): Long {
    val now = System.currentTimeMillis()
    return if (card.id == 0L) {
      val favOrder = if (card.isFavorite) dao.getMaxFavoriteOrder() + 1 else 0
      dao.insertCard(
        card.copy(
          favoriteOrder = favOrder,
          createdAt = now,
          updatedAt = now
        )
      )
    } else {
      dao.updateCard(card.copy(updatedAt = now))
      card.id
    }
  }

  suspend fun deleteCard(card: PhysicalCard) {
    dao.deleteCard(card)
  }

  suspend fun renameCard(cardId: Long, newTitle: String) {
    val trimmed = newTitle.trim()
    if (trimmed.isNotEmpty()) {
      dao.renameCard(cardId, trimmed)
    }
  }

  suspend fun toggleFavorite(card: PhysicalCard) {
    val newFav = !card.isFavorite
    val nextOrder = if (newFav) dao.getMaxFavoriteOrder() + 1 else 0
    dao.setCardFavorite(card.id, newFav, nextOrder)
  }

  suspend fun reorderFavorites(orderedCardIds: List<Long>) {
    dao.updateFavoritesOrder(orderedCardIds)
  }

  suspend fun saveCategory(category: CardCategory): Long {
    return if (category.id == 0L) {
      val count = dao.getCategoryCount()
      dao.insertCategory(category.copy(sortOrder = count))
    } else {
      dao.updateCategory(category)
      category.id
    }
  }

  suspend fun deleteCategory(category: CardCategory, fallbackCategoryId: Long = 1L) {
    dao.reassignCardsToCategory(category.id, fallbackCategoryId)
    dao.deleteCategory(category)
  }
}
