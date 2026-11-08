package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.example.util.AppIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.CategoryIconMapper

@Composable
fun FavoritesScreen(
  favoriteCards: List<PhysicalCard>,
  categories: List<CardCategory>,
  onCardClick: (PhysicalCard) -> Unit,
  onRenameCard: (PhysicalCard) -> Unit,
  onMoveUp: (Int) -> Unit,
  onMoveDown: (Int) -> Unit,
  onMoveToTop: (Long) -> Unit,
  onUnfavorite: (PhysicalCard) -> Unit,
  modifier: Modifier = Modifier
) {
  val categoryMap = categories.associateBy { it.id }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Pinned Favorites",
      style = MaterialTheme.typography.headlineLarge,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = stringResource(R.string.favorites_reorder_hint),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (favoriteCards.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = "No Favorites",
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = stringResource(R.string.empty_favorites_title),
              style = MaterialTheme.typography.titleLarge,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(R.string.empty_favorites_subtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("favorites_reorder_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
      ) {
        itemsIndexed(
          items = favoriteCards,
          key = { _, card -> card.id }
        ) { index, card ->
          val category = categoryMap[card.categoryId]
          val accentColor = Color(card.colorHex)

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCardClick(card) }
              .testTag("favorite_reorder_item_${card.id}"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      accentColor.copy(alpha = 0.18f),
                      Color.Transparent
                    )
                  )
                )
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Rank Badge
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(accentColor.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#${index + 1}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = accentColor
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              // Card details
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = CategoryIconMapper.getIcon(category?.iconName ?: "card"),
                    contentDescription = category?.name,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = category?.name ?: "Card",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = card.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                val codeDisplay = card.cardNumber.ifBlank { card.barcodeValue }
                if (codeDisplay.isNotBlank()) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = codeDisplay,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Reorganize & Rename Action Controls
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { onRenameCard(card) },
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_rename_button_${card.id}")
                ) {
                  Icon(
                    imageVector = AppIcons.DriveFileRenameOutline,
                    contentDescription = "Rename ${card.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }

                if (index > 1) {
                  IconButton(
                    onClick = { onMoveToTop(card.id) },
                    modifier = Modifier
                      .size(40.dp)
                      .testTag("fav_move_top_button_${card.id}")
                  ) {
                    Icon(
                      imageVector = AppIcons.VerticalAlignTop,
                      contentDescription = "Move to Top",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                IconButton(
                  onClick = { onMoveUp(index) },
                  enabled = index > 0,
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_move_up_button_${card.id}")
                ) {
                  Icon(
                    imageVector = AppIcons.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                  )
                }

                IconButton(
                  onClick = { onMoveDown(index) },
                  enabled = index < favoriteCards.lastIndex,
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_move_down_button_${card.id}")
                ) {
                  Icon(
                    imageVector = AppIcons.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = if (index < favoriteCards.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                  )
                }

                IconButton(
                  onClick = { onUnfavorite(card) },
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_unpin_button_${card.id}")
                ) {
                  Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Unpin Favorite",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
