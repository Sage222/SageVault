package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.example.util.AppIcons
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BarcodeFormatType
import com.example.data.PhysicalCard
import com.example.ui.SageVaultViewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.RenameCardDialog
import com.example.ui.screens.AddEditCardDialog
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CategoryEditorDialog
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.ScannerPassModal
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.SageVaultTheme

enum class SageVaultTab(val route: String, val label: String) {
  VAULT("vault", "Vault"),
  FAVORITES("favorites", "Favorites"),
  CATEGORIES("categories", "Categories")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SageVaultTheme {
        SageVaultApp()
      }
    }
  }
}

@Composable
fun SageVaultApp(
  viewModel: SageVaultViewModel = viewModel(factory = SageVaultViewModel.Factory)
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var currentTab by remember { mutableStateOf(SageVaultTab.VAULT) }
  var selectedPassCardId by remember { mutableStateOf<Long?>(null) }
  var cardToRename by remember { mutableStateOf<PhysicalCard?>(null) }
  var cardBeingEdited by remember { mutableStateOf<PhysicalCard?>(null) }
  var showAddEditDialog by remember { mutableStateOf(false) }
  var showQuickScanner by remember { mutableStateOf(false) }
  var scannedCodeForNewCard by remember { mutableStateOf<String?>(null) }
  var scannedFormatForNewCard by remember { mutableStateOf<BarcodeFormatType?>(null) }
  var showQuickNewCategoryDialog by remember { mutableStateOf(false) }

  // Keep selected card reactive to database updates (e.g. rename, favorite toggle, photo edits)
  val activePassCard = remember(uiState.allCards, selectedPassCardId) {
    selectedPassCardId?.let { id -> uiState.allCards.find { it.id == id } }
  }

  // Handle back button when on secondary tabs
  if (currentTab != SageVaultTab.VAULT) {
    BackHandler {
      currentTab = SageVaultTab.VAULT
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("bottom_navigation_bar")
      ) {
        NavigationBarItem(
          selected = currentTab == SageVaultTab.VAULT,
          onClick = { currentTab = SageVaultTab.VAULT },
          icon = {
            Icon(
              imageVector = AppIcons.CreditCard,
              contentDescription = "Vault"
            )
          },
          label = { Text("Vault") },
          modifier = Modifier.testTag("nav_tab_vault")
        )

        NavigationBarItem(
          selected = currentTab == SageVaultTab.FAVORITES,
          onClick = { currentTab = SageVaultTab.FAVORITES },
          icon = {
            Icon(
              imageVector = if (currentTab == SageVaultTab.FAVORITES) {
                Icons.Filled.Star
              } else {
                AppIcons.StarBorder
              },
              contentDescription = "Favorites"
            )
          },
          label = { Text("Favorites") },
          modifier = Modifier.testTag("nav_tab_favorites")
        )

        NavigationBarItem(
          selected = currentTab == SageVaultTab.CATEGORIES,
          onClick = { currentTab = SageVaultTab.CATEGORIES },
          icon = {
            Icon(
              imageVector = AppIcons.Category,
              contentDescription = "Categories"
            )
          },
          label = { Text("Categories") },
          modifier = Modifier.testTag("nav_tab_categories")
        )
      }
    },
    floatingActionButton = {
      if (currentTab != SageVaultTab.CATEGORIES) {
        ExtendedFloatingActionButton(
          onClick = {
            cardBeingEdited = null
            scannedCodeForNewCard = null
            scannedFormatForNewCard = null
            showAddEditDialog = true
          },
          icon = {
            Icon(
              imageVector = AppIcons.AddCard,
              contentDescription = "Add Physical Card"
            )
          },
          text = { Text("Add Card") },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("fab_add_card")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        SageVaultTab.VAULT -> {
          VaultHomeScreen(
            uiState = uiState,
            onSearchQueryChange = viewModel::setSearchQuery,
            onSelectCategory = viewModel::selectCategory,
            onCardClick = { card -> selectedPassCardId = card.id },
            onToggleFavorite = viewModel::toggleFavorite,
            onRenameCard = { card -> cardToRename = card },
            onQuickScanClick = { showQuickScanner = true },
            onAddCardClick = {
              cardBeingEdited = null
              scannedCodeForNewCard = null
              scannedFormatForNewCard = null
              showAddEditDialog = true
            },
            onAddCategoryClick = { showQuickNewCategoryDialog = true },
            onNavigateToFavoritesReorder = { currentTab = SageVaultTab.FAVORITES }
          )
        }

        SageVaultTab.FAVORITES -> {
          FavoritesScreen(
            favoriteCards = uiState.favoriteCards,
            categories = uiState.categories,
            onCardClick = { card -> selectedPassCardId = card.id },
            onRenameCard = { card -> cardToRename = card },
            onMoveUp = { index -> viewModel.moveFavorite(index, index - 1) },
            onMoveDown = { index -> viewModel.moveFavorite(index, index + 1) },
            onMoveToTop = { cardId -> viewModel.moveFavoriteToTop(cardId) },
            onUnfavorite = viewModel::toggleFavorite
          )
        }

        SageVaultTab.CATEGORIES -> {
          CategoriesScreen(
            categories = uiState.categories,
            allCards = uiState.allCards,
            onSelectCategoryFilter = { categoryId ->
              viewModel.selectCategory(categoryId)
              currentTab = SageVaultTab.VAULT
            },
            onSaveCategory = { category -> viewModel.saveCategory(category) },
            onDeleteCategory = viewModel::deleteCategory
          )
        }
      }
    }
  }

  // Fullscreen Scanner Pass & Card Photo Modal
  if (activePassCard != null) {
    val category = uiState.categories.find { it.id == activePassCard.categoryId }
    ScannerPassModal(
      card = activePassCard,
      category = category,
      onDismiss = { selectedPassCardId = null },
      onToggleFavorite = { viewModel.toggleFavorite(activePassCard) },
      onRenameClick = { cardToRename = activePassCard },
      onEditClick = {
        cardBeingEdited = activePassCard
        scannedCodeForNewCard = null
        scannedFormatForNewCard = null
        showAddEditDialog = true
      },
      onDeleteCard = {
        val cardToDelete = activePassCard
        selectedPassCardId = null
        viewModel.deleteCard(cardToDelete)
      }
    )
  }

  // Add / Edit Card Dialog
  if (showAddEditDialog) {
    AddEditCardDialog(
      initialCard = cardBeingEdited,
      categories = uiState.categories,
      initialScannedCode = scannedCodeForNewCard,
      initialScannedFormat = scannedFormatForNewCard,
      onDismiss = {
        showAddEditDialog = false
        cardBeingEdited = null
      },
      onSaveCard = { savedCard ->
        viewModel.saveCard(savedCard) { newId ->
          showAddEditDialog = false
          cardBeingEdited = null
          selectedPassCardId = newId
        }
      },
      onOpenNewCategoryDialog = { showQuickNewCategoryDialog = true }
    )
  }

  // Quick Barcode Scanner Dialog from Home Header
  if (showQuickScanner) {
    BarcodeScannerDialog(
      onDismiss = { showQuickScanner = false },
      onBarcodeScanned = { scanned ->
        showQuickScanner = false
        cardBeingEdited = null
        scannedCodeForNewCard = scanned.rawValue
        scannedFormatForNewCard = scanned.formatType
        showAddEditDialog = true
      }
    )
  }

  // Quick Rename Dialog
  if (cardToRename != null) {
    RenameCardDialog(
      card = cardToRename!!,
      onDismiss = { cardToRename = null },
      onConfirmRename = { newName ->
        viewModel.renameCard(cardToRename!!.id, newName)
        cardToRename = null
      }
    )
  }

  // Quick New Custom Category Dialog
  if (showQuickNewCategoryDialog) {
    CategoryEditorDialog(
      initialCategory = null,
      onDismiss = { showQuickNewCategoryDialog = false },
      onSave = { newCategory ->
        viewModel.saveCategory(newCategory)
        showQuickNewCategoryDialog = false
      }
    )
  }
}
