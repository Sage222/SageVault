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

  suspend fun restoreBackup(
    backupCategories: List<CardCategory>,
    backupCards: List<PhysicalCard>
  ): Int {
    val existingCategories = dao.getAllCategoriesOnce().toMutableList()
    val categoryIdMap = mutableMapOf<Long, Long>()

    // 1. Restore/map categories by case-insensitive name so cards link to the right category across devices
    for (incomingCat in backupCategories) {
      val match = existingCategories.find { it.name.equals(incomingCat.name, ignoreCase = true) }
      if (match != null) {
        val updated = match.copy(
          iconName = incomingCat.iconName,
          colorHex = incomingCat.colorHex
        )
        dao.updateCategory(updated)
        categoryIdMap[incomingCat.id] = match.id
      } else {
        val insertedId = dao.insertCategory(
          incomingCat.copy(
            id = 0L,
            sortOrder = existingCategories.size
          )
        )
        val created = incomingCat.copy(id = insertedId)
        existingCategories.add(created)
        categoryIdMap[incomingCat.id] = insertedId
      }
    }

    val defaultCategoryId = existingCategories.firstOrNull()?.id ?: 1L
    val existingCards = dao.getAllCardsOnce().toMutableList()
    var nextFavOrder = (existingCards.filter { it.isFavorite }.maxOfOrNull { it.favoriteOrder } ?: 0) + 1
    var restoredCount = 0

    // 2. Restore all cards (updating existing matching card or inserting new card)
    for (incomingCard in backupCards) {
      val mappedCategoryId = categoryIdMap[incomingCard.categoryId] ?: defaultCategoryId
      val existingMatch = existingCards.find {
        it.title.equals(incomingCard.title, ignoreCase = true) &&
          it.cardNumber == incomingCard.cardNumber &&
          it.barcodeValue == incomingCard.barcodeValue
      }

      if (existingMatch != null) {
        val updatedCard = existingMatch.copy(
          title = incomingCard.title,
          subtitle = incomingCard.subtitle,
          cardNumber = incomingCard.cardNumber,
          barcodeValue = incomingCard.barcodeValue,
          barcodeFormat = incomingCard.barcodeFormat,
          categoryId = mappedCategoryId,
          colorHex = incomingCard.colorHex,
          frontImagePath = incomingCard.frontImagePath ?: existingMatch.frontImagePath,
          backImagePath = incomingCard.backImagePath ?: existingMatch.backImagePath,
          notes = incomingCard.notes,
          isFavorite = incomingCard.isFavorite,
          updatedAt = System.currentTimeMillis()
        )
        dao.updateCard(updatedCard)
        restoredCount++
      } else {
        val favOrder = if (incomingCard.isFavorite) {
          nextFavOrder++
        } else {
          0
        }
        val insertedId = dao.insertCard(
          incomingCard.copy(
            id = 0L,
            categoryId = mappedCategoryId,
            favoriteOrder = favOrder,
            updatedAt = System.currentTimeMillis()
          )
        )
        existingCards.add(incomingCard.copy(id = insertedId, categoryId = mappedCategoryId))
        restoredCount++
      }
    }

    return restoredCount
  }
}
