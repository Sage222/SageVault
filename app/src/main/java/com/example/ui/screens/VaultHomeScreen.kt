package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PhysicalCard
import com.example.ui.VaultUiState
import com.example.ui.components.FavoriteQuickChipCard
import com.example.ui.components.VaultCardItem
import com.example.util.AppIcons
import com.example.util.CategoryIconMapper

private const val GITHUB_REPO_URL = "https://github.com/Sage222/SageVault"

@Composable
fun VaultHomeScreen(
  uiState: VaultUiState,
  onSearchQueryChange: (String) -> Unit,
  onSelectCategory: (Long?) -> Unit,
  onCardClick: (PhysicalCard) -> Unit,
  onToggleFavorite: (PhysicalCard) -> Unit,
  onRenameCard: (PhysicalCard) -> Unit,
  onDeleteCard: (PhysicalCard) -> Unit,
  onAddCardClick: () -> Unit,
  onAddCategoryClick: () -> Unit,
  onNavigateToFavoritesReorder: () -> Unit,
  modifier: Modifier = Modifier
) {
  val categoryMap = uiState.categories.associateBy { it.id }
  val uriHandler = LocalUriHandler.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("vault_cards_list"),
    contentPadding = PaddingValues(bottom = 110.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Top Header Bar (Clicking SageVault opens the GitHub page; top Scan Card button removed)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
              try {
                uriHandler.openUri(GITHUB_REPO_URL)
              } catch (_: Exception) {
              }
            }
            .padding(vertical = 2.dp)
            .testTag("sagevault_github_header")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = "${uiState.allCards.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. Search Bar (clean, no placeholder text)
    item {
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onSearchQueryChange,
        leadingIcon = {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        trailingIcon = {
          if (uiState.searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchQueryChange("") },
              modifier = Modifier.testTag("clear_search_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "Clear Search"
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .testTag("vault_search_input")
      )
    }

    // 3. Category Filter Strip
    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("category_filter_row")
      ) {
        item {
          FilterChip(
            selected = uiState.selectedCategoryId == null,
            onClick = { onSelectCategory(null) },
            label = { Text(stringResource(R.string.all_categories)) },
            modifier = Modifier.testTag("filter_chip_all")
          )
        }

        items(
          items = uiState.categories,
          key = { it.id }
        ) { category ->
          val selected = uiState.selectedCategoryId == category.id
          FilterChip(
            selected = selected,
            onClick = {
              onSelectCategory(if (selected) null else category.id)
            },
            label = { Text(category.name) },
            leadingIcon = {
              Icon(
                imageVector = CategoryIconMapper.getIcon(category.iconName),
                contentDescription = category.name,
                tint = Color(category.colorHex),
                modifier = Modifier.size(16.dp)
              )
            },
            modifier = Modifier.testTag("filter_chip_category_${category.id}")
          )
        }

        item {
          FilterChip(
            selected = false,
            onClick = onAddCategoryClick,
            label = { Text("Custom") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Category",
                modifier = Modifier.size(16.dp)
              )
            },
            modifier = Modifier.testTag("filter_chip_add_category")
          )
        }
      }
    }

    // 4. Pinned Favorites Horizontal Strip (when showing All Cards and Favorites exist)
    if (uiState.favoriteCards.isNotEmpty() &&
      uiState.selectedCategoryId == null &&
      uiState.searchQuery.isBlank()
    ) {
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Favorites",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = stringResource(R.string.favorites_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
              )
            }

            TextButton(
              onClick = onNavigateToFavoritesReorder,
              modifier = Modifier.testTag("reorganize_favorites_link")
            ) {
              Icon(
                imageVector = AppIcons.SwapVert,
                contentDescription = "Reorganize Favorites",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reorganize")
            }
          }

          LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("favorites_quick_row")
          ) {
            itemsIndexed(
              items = uiState.favoriteCards,
              key = { _, card -> "fav_${card.id}" }
            ) { index, favCard ->
              FavoriteQuickChipCard(
                card = favCard,
                category = categoryMap[favCard.categoryId],
                rank = index + 1,
                onClick = { onCardClick(favCard) }
              )
            }
          }
        }
      }
    }

    // 5. Section Header for Cards
    item {
      val activeCategoryName = uiState.selectedCategoryId?.let { categoryMap[it]?.name }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = activeCategoryName ?: "All Physical Cards",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "${uiState.filteredCards.size} shown",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 6. Card Items or Empty State
    if (uiState.filteredCards.isEmpty()) {
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_vault_hero_1794178751354),
                contentDescription = "SageVault Card Illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
                      )
                    )
                  )
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = if (uiState.searchQuery.isNotBlank()) {
                "No Matching Cards Found"
              } else {
                stringResource(R.string.empty_vault_title)
              },
              style = MaterialTheme.typography.titleLarge,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = if (uiState.searchQuery.isNotBlank()) {
                "Try clearing your search or switching category filters."
              } else {
                stringResource(R.string.empty_vault_subtitle)
              },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = onAddCardClick,
              modifier = Modifier.testTag("empty_state_add_manual_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Card",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Card")
            }
          }
        }
      }
    } else {
      items(
        items = uiState.filteredCards,
        key = { it.id }
      ) { card ->
        VaultCardItem(
          card = card,
          category = categoryMap[card.categoryId],
          onClick = { onCardClick(card) },
          onToggleFavorite = { onToggleFavorite(card) },
          onRenameClick = { onRenameCard(card) },
          onDeleteClick = { onDeleteCard(card) },
          modifier = Modifier.padding(horizontal = 20.dp)
        )
      }
    }
  }
}
