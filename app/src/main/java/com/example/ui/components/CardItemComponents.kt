package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.BarcodeFormatType
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.CategoryIconMapper
import java.io.File

@Composable
fun VaultCardItem(
  card: PhysicalCard,
  category: CardCategory?,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRenameClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = Color(card.colorHex)
  val formatType = BarcodeFormatType.fromName(card.barcodeFormat)
  val photoCount = listOfNotNull(card.frontImagePath, card.backImagePath).count { it.isNotBlank() }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("card_item_${card.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    border = BorderStroke(
      width = 1.dp,
      brush = Brush.horizontalGradient(
        colors = listOf(
          accentColor.copy(alpha = 0.65f),
          MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              accentColor.copy(alpha = 0.16f),
              Color.Transparent
            )
          )
        )
        .padding(18.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Top row: Category pill + actions (Rename & Favorite)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = accentColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = CategoryIconMapper.getIcon(category?.iconName ?: "card"),
                contentDescription = category?.name ?: "Category",
                tint = accentColor,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = category?.name ?: "Card",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onRenameClick,
              modifier = Modifier
                .size(40.dp)
                .testTag("rename_card_button_${card.id}")
            ) {
              Icon(
                imageVector = Icons.Filled.DriveFileRenameOutline,
                contentDescription = "Rename ${card.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = onToggleFavorite,
              modifier = Modifier
                .size(40.dp)
                .testTag("favorite_card_button_${card.id}")
            ) {
              Icon(
                imageVector = if (card.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (card.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (card.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main content: Title, subtitle, and optional thumbnail preview if card has a photo
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = card.title,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (card.subtitle.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          if (!card.frontImagePath.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(12.dp))
            AsyncImage(
              model = File(card.frontImagePath),
              contentDescription = "${card.title} front photo thumbnail",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(width = 64.dp, height = 42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom metadata bar: Card number + barcode/photo badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val displayCode = when {
            card.cardNumber.isNotBlank() -> card.cardNumber
            card.barcodeValue.isNotBlank() -> card.barcodeValue
            else -> "PHOTO PASS"
          }
          Text(
            text = displayCode,
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (photoCount > 0) {
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Card Photos",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$photoCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            if (card.barcodeValue.isNotBlank() && formatType != BarcodeFormatType.NONE) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = "Scannable Code",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (formatType.is2D) "2D" else "1D",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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

@Composable
fun FavoriteQuickChipCard(
  card: PhysicalCard,
  category: CardCategory?,
  rank: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = Color(card.colorHex)

  Surface(
    modifier = modifier
      .width(210.dp)
      .clickable(onClick = onClick)
      .testTag("favorite_quick_card_${card.id}"),
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .background(
          Brush.verticalGradient(
            colors = listOf(
              accentColor.copy(alpha = 0.22f),
              Color.Transparent
            )
          )
        )
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .background(accentColor.copy(alpha = 0.25f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = CategoryIconMapper.getIcon(category?.iconName ?: "card"),
            contentDescription = category?.name,
            tint = accentColor,
            modifier = Modifier.size(15.dp)
          )
        }

        Surface(
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = card.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = category?.name ?: card.subtitle.ifBlank { "Tap to scan" },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = card.cardNumber.ifBlank { card.barcodeValue.ifBlank { "Open Card" } },
        fontFamily = JetBrainsMonoFontFamily,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun RenameCardDialog(
  card: PhysicalCard,
  onDismiss: () -> Unit,
  onConfirmRename: (String) -> Unit
) {
  var titleText by remember(card.id) { mutableStateOf(card.title) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Rename Card",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column {
        Text(
          text = "Enter a clean, recognizable name for this card:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = titleText,
          onValueChange = { titleText = it },
          label = { Text("Card Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rename_card_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (titleText.isNotBlank()) {
            onConfirmRename(titleText.trim())
          }
        },
        enabled = titleText.isNotBlank(),
        modifier = Modifier.testTag("confirm_rename_button")
      ) {
        Text("Save Name")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_rename_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
