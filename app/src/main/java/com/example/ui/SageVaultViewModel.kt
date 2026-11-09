package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.data.SageVaultDatabase
import com.example.data.SageVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
  val allCards: List<PhysicalCard> = emptyList(),
  val filteredCards: List<PhysicalCard> = emptyList(),
  val favoriteCards: List<PhysicalCard> = emptyList(),
  val categories: List<CardCategory> = emptyList(),
  val selectedCategoryId: Long? = null,
  val searchQuery: String = ""
)

class SageVaultViewModel(
  application: Application,
  private val repository: SageVaultRepository
) : AndroidViewModel(application) {

  private val _selectedCategoryId = MutableStateFlow<Long?>(null)
  val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  val uiState: StateFlow<VaultUiState> = combine(
    repository.allCards,
    repository.favoriteCards,
    repository.categories,
    _selectedCategoryId,
    _searchQuery
  ) { allCards, favCards, categories, selectedCategoryId, query ->
    val trimmedQuery = query.trim().lowercase()
    val filtered = allCards.filter { card ->
      val matchesCategory = selectedCategoryId == null || card.categoryId == selectedCategoryId
      val matchesQuery = if (trimmedQuery.isEmpty()) {
        true
      } else {
        card.title.lowercase().contains(trimmedQuery) ||
          card.subtitle.lowercase().contains(trimmedQuery) ||
          card.cardNumber.lowercase().contains(trimmedQuery) ||
          card.barcodeValue.lowercase().contains(trimmedQuery) ||
          card.notes.lowercase().contains(trimmedQuery)
      }
      matchesCategory && matchesQuery
    }
    VaultUiState(
      allCards = allCards,
      filteredCards = filtered,
      favoriteCards = favCards,
      categories = categories,
      selectedCategoryId = selectedCategoryId,
      searchQuery = query
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = VaultUiState()
  )

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectCategory(categoryId: Long?) {
    _selectedCategoryId.value = categoryId
  }

  fun saveCard(card: PhysicalCard, onSaved: (Long) -> Unit = {}) {
    viewModelScope.launch {
      val id = repository.saveCard(card)
      onSaved(id)
    }
  }

  fun deleteCard(card: PhysicalCard) {
    viewModelScope.launch {
      repository.deleteCard(card)
    }
  }

  fun renameCard(cardId: Long, newTitle: String) {
    viewModelScope.launch {
      repository.renameCard(cardId, newTitle)
    }
  }

  fun toggleFavorite(card: PhysicalCard) {
    viewModelScope.launch {
      repository.toggleFavorite(card)
    }
  }

  fun moveFavorite(fromIndex: Int, toIndex: Int) {
    val currentFavs = uiState.value.favoriteCards.toMutableList()
    if (fromIndex !in currentFavs.indices || toIndex !in currentFavs.indices || fromIndex == toIndex) {
      return
    }
    val item = currentFavs.removeAt(fromIndex)
    currentFavs.add(toIndex, item)
    viewModelScope.launch {
      repository.reorderFavorites(currentFavs.map { it.id })
    }
  }

  fun moveFavoriteToTop(cardId: Long) {
    val currentFavs = uiState.value.favoriteCards.toMutableList()
    val index = currentFavs.indexOfFirst { it.id == cardId }
    if (index <= 0) return
    val item = currentFavs.removeAt(index)
    currentFavs.add(0, item)
    viewModelScope.launch {
      repository.reorderFavorites(currentFavs.map { it.id })
    }
  }

  fun saveCategory(category: CardCategory, onSaved: (Long) -> Unit = {}) {
    viewModelScope.launch {
      val id = repository.saveCategory(category)
      onSaved(id)
    }
  }

  fun deleteCategory(category: CardCategory) {
    viewModelScope.launch {
      val fallbackId = uiState.value.categories.firstOrNull { it.id != category.id }?.id ?: 1L
      if (_selectedCategoryId.value == category.id) {
        _selectedCategoryId.value = null
      }
      repository.deleteCategory(category, fallbackId)
    }
  }

  fun restoreFromBackup(
    backupCategories: List<CardCategory>,
    backupCards: List<PhysicalCard>,
    onComplete: (Int) -> Unit
  ) {
    viewModelScope.launch {
      val count = repository.restoreBackup(backupCategories, backupCards)
      onComplete(count)
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application =
          (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
        val dao = SageVaultDatabase.getDatabase(application).sageVaultDao()
        val repository = SageVaultRepository(dao)
        SageVaultViewModel(application, repository)
      }
    }
  }
}
