package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.theme.CardPalette
import com.example.util.AppIcons
import com.example.util.CategoryIconMapper
import com.example.util.VaultBackupManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  categories: List<CardCategory>,
  allCards: List<PhysicalCard>,
  onSelectCategoryFilter: (Long) -> Unit,
  onSaveCategory: (CardCategory) -> Unit,
  onDeleteCategory: (CardCategory) -> Unit,
  onRestoreBackup: (List<CardCategory>, List<PhysicalCard>, (Int) -> Unit) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var editingCategory by remember { mutableStateOf<CardCategory?>(null) }
  var showCategoryEditor by remember { mutableStateOf(false) }
  var isBusy by remember { mutableStateOf(false) }
  var backupStatusMessage by remember { mutableStateOf<String?>(null) }

  // 1. Export Backup File Launcher (Saves to Downloads, Google Drive, SD card, etc.)
  val createBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { targetUri: Uri? ->
    if (targetUri != null) {
      isBusy = true
      backupStatusMessage = "Exporting all cards and photos..."
      coroutineScope.launch {
        val result = VaultBackupManager.exportBackupToUri(
          context = context,
          targetUri = targetUri,
          categories = categories,
          cards = allCards
        )
        isBusy = false
        result.fold(
          onSuccess = { count ->
            val msg = "Backup saved ($count ${if (count == 1) "card" else "cards"} with photos & categories)."
            backupStatusMessage = msg
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          },
          onFailure = { err ->
            val msg = "Backup failed: ${err.localizedMessage ?: "Unknown error"}"
            backupStatusMessage = msg
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
          }
        )
      }
    }
  }

  // 2. Restore Backup File Launcher (Loads from Downloads, Google Drive, or transferred file)
  val restoreBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { sourceUri: Uri? ->
    if (sourceUri != null) {
      isBusy = true
      backupStatusMessage = "Restoring cards and photos from backup..."
      coroutineScope.launch {
        val parseResult = VaultBackupManager.parseBackupFromUri(context, sourceUri)
        parseResult.fold(
          onSuccess = { payload ->
            onRestoreBackup(payload.categories, payload.cards) { restoredCount ->
              isBusy = false
              val msg =
                "Restored $restoredCount ${if (restoredCount == 1) "card" else "cards"} and ${payload.categories.size} categories."
              backupStatusMessage = msg
              Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
          },
          onFailure = { err ->
            isBusy = false
            val msg = "Restore failed: ${err.localizedMessage ?: "Invalid backup file"}"
            backupStatusMessage = msg
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
          }
        )
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
      .testTag("settings_list"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
  ) {
    // 1. Settings Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Settings",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Backup & restore your entire vault across devices, and manage custom categories.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. Cross-Device Backup & Restore Card
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("backup_restore_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = AppIcons.FolderSpecial,
                contentDescription = "Backup & Restore",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Backup & Restore All Cards",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${allCards.size} ${if (allCards.size == 1) "card" else "cards"} • ${categories.size} categories (includes Front & Back photos)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = "Creates a single portable backup file containing all your cards, barcodes, custom categories, and front/back card photos so you can restore your vault on any phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                createBackupLauncher.launch(VaultBackupManager.defaultBackupFileName())
              },
              enabled = !isBusy,
              modifier = Modifier
                .weight(1f)
                .testTag("backup_all_cards_button")
            ) {
              Text("Backup All")
            }

            FilledTonalButton(
              onClick = {
                restoreBackupLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
              },
              enabled = !isBusy,
              modifier = Modifier
                .weight(1f)
                .testTag("restore_all_cards_button")
            ) {
              Text("Restore All")
            }
          }

          OutlinedButton(
            onClick = {
              isBusy = true
              coroutineScope.launch {
                val shareResult = VaultBackupManager.createShareableBackupIntent(
                  context = context,
                  categories = categories,
                  cards = allCards
                )
                isBusy = false
                shareResult.fold(
                  onSuccess = { chooserIntent ->
                    context.startActivity(chooserIntent)
                  },
                  onFailure = { err ->
                    Toast.makeText(
                      context,
                      "Could not share backup: ${err.localizedMessage ?: "Error"}",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                )
              }
            },
            enabled = !isBusy,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("share_backup_across_devices_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Share,
              contentDescription = "Share Backup",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Backup to Another Device")
          }

          if (!backupStatusMessage.isNullOrBlank()) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = backupStatusMessage!!,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }
      }
    }

    // 3. Custom Categories Section Header inside Settings
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Custom Categories",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Tap a category to filter your vault, or add new categories.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
          onClick = {
            editingCategory = null
            showCategoryEditor = true
          },
          modifier = Modifier.testTag("new_category_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Category",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("New")
        }
      }
    }

    // 4. Category Items List
    items(
      items = categories,
      key = { it.id }
    ) { category ->
      val cardCount = allCards.count { it.categoryId == category.id }
      val accentColor = Color(category.colorHex)

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectCategoryFilter(category.id) }
          .testTag("category_row_${category.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .background(accentColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = CategoryIconMapper.getIcon(category.iconName),
              contentDescription = category.name,
              tint = accentColor,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = category.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (cardCount == 1) "1 card stored" else "$cardCount cards stored",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                editingCategory = category
                showCategoryEditor = true
              },
              modifier = Modifier.testTag("edit_category_button_${category.id}")
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit ${category.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (categories.size > 1) {
              IconButton(
                onClick = { onDeleteCategory(category) },
                modifier = Modifier.testTag("delete_category_button_${category.id}")
              ) {
                Icon(
                  imageVector = AppIcons.DeleteOutline,
                  contentDescription = "Delete ${category.name}",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                )
              }
            }
          }
        }
      }
    }
  }

  if (showCategoryEditor) {
    CategoryEditorDialog(
      initialCategory = editingCategory,
      onDismiss = { showCategoryEditor = false },
      onSave = { updated ->
        onSaveCategory(updated)
        showCategoryEditor = false
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
  initialCategory: CardCategory?,
  onDismiss: () -> Unit,
  onSave: (CardCategory) -> Unit
) {
  var name by remember { mutableStateOf(initialCategory?.name ?: "") }
  var selectedIcon by remember { mutableStateOf(initialCategory?.iconName ?: "custom") }
  var selectedColor by remember {
    mutableLongStateOf(initialCategory?.colorHex ?: CardPalette.SAGE_EMERALD)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialCategory == null) "New Custom Category" else "Edit Category",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Category Name (e.g., Museum, Dental, Coworking)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("category_name_input")
        )

        Text(
          text = "Choose Icon",
          style = MaterialTheme.typography.labelLarge
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          CategoryIconMapper.iconOptions.forEach { option ->
            FilterChip(
              selected = selectedIcon == option.key,
              onClick = { selectedIcon = option.key },
              label = { Text(option.label) },
              leadingIcon = {
                Icon(
                  imageVector = option.icon,
                  contentDescription = option.label,
                  modifier = Modifier.size(16.dp)
                )
              },
              modifier = Modifier.testTag("category_icon_chip_${option.key}")
            )
          }
        }

        Text(
          text = "Accent Color",
          style = MaterialTheme.typography.labelLarge
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CardPalette.allColors.forEach { colorHex ->
            val isSelected = selectedColor == colorHex
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(colorHex))
                .border(
                  width = if (isSelected) 2.5.dp else 0.dp,
                  color = if (isSelected) Color.White else Color.Transparent,
                  shape = CircleShape
                )
                .clickable { selectedColor = colorHex },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Selected",
                  tint = Color.Black,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val category = (initialCategory ?: CardCategory(
              name = name.trim(),
              iconName = selectedIcon,
              colorHex = selectedColor
            )).copy(
              name = name.trim(),
              iconName = selectedIcon,
              colorHex = selectedColor
            )
            onSave(category)
          }
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("save_category_button")
      ) {
        Text("Save Category")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
