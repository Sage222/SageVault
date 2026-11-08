package com.example.ui.screens

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
import com.example.util.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.theme.CardPalette
import com.example.util.CategoryIconMapper

@Composable
fun CategoriesScreen(
  categories: List<CardCategory>,
  allCards: List<PhysicalCard>,
  onSelectCategoryFilter: (Long) -> Unit,
  onSaveCategory: (CardCategory) -> Unit,
  onDeleteCategory: (CardCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  var editingCategory by remember { mutableStateOf<CardCategory?>(null) }
  var showCategoryEditor by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Custom Categories",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Organize library, gym, insurance, and custom membership cards.",
          style = MaterialTheme.typography.bodyMedium,
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

    Spacer(modifier = Modifier.height(18.dp))

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("categories_list"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
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
