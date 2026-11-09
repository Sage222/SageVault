package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BarcodeFormatType
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.components.CardPhotoCropDialog
import com.example.ui.components.DeleteCardConfirmDialog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.AppIcons
import com.example.util.BarcodeGenerator
import com.example.util.CategoryIconMapper
import com.example.util.LocalCardImage
import java.io.File

@Composable
fun ScannerPassModal(
  card: PhysicalCard,
  category: CardCategory?,
  onDismiss: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRenameClick: () -> Unit,
  onEditClick: () -> Unit,
  onUpdateCard: (PhysicalCard) -> Unit,
  onDeleteCard: () -> Unit
) {
  BackHandler { onDismiss() }

  val context = LocalContext.current
  val view = LocalView.current
  val accentColor = Color(card.colorHex)
  val formatType = BarcodeFormatType.fromName(card.barcodeFormat)

  var fullscreenImagePath by remember { mutableStateOf<String?>(null) }
  var fullscreenSide by remember { mutableStateOf<String?>(null) }
  var cropTargetSide by remember { mutableStateOf<String?>(null) }
  var cropTargetPath by remember { mutableStateOf<String?>(null) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  // Automatically turn screen brightness to 100% when viewing a card pass,
  // and restore normal brightness immediately when leaving this screen.
  DisposableEffect(Unit) {
    val window = (view.context as? Activity)?.window
    val previousBrightness = window?.attributes?.screenBrightness ?: -1f
    if (window != null) {
      val params = window.attributes
      params.screenBrightness = 1.0f
      window.attributes = params
    }
    onDispose {
      if (window != null) {
        val params = window.attributes
        params.screenBrightness = previousBrightness
        window.attributes = params
      }
    }
  }

  val barcodeBitmap = remember(card.barcodeValue, card.barcodeFormat) {
    if (card.barcodeValue.isNotBlank() && formatType != BarcodeFormatType.NONE) {
      BarcodeGenerator.generateBarcodeBitmap(card.barcodeValue, formatType)
    } else {
      null
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
      ) {
        // Top App Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_pass_modal_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Vault"
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onRenameClick,
              modifier = Modifier.testTag("pass_rename_button")
            ) {
              Icon(
                imageVector = AppIcons.DriveFileRenameOutline,
                contentDescription = "Rename Card"
              )
            }
            IconButton(
              onClick = onToggleFavorite,
              modifier = Modifier.testTag("pass_favorite_button")
            ) {
              Icon(
                imageVector = if (card.isFavorite) Icons.Filled.Star else AppIcons.StarBorder,
                contentDescription = "Toggle Favorite",
                tint = if (card.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
              )
            }
            IconButton(
              onClick = onEditClick,
              modifier = Modifier.testTag("pass_edit_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit Card"
              )
            }
            IconButton(
              onClick = { showDeleteConfirm = true },
              modifier = Modifier.testTag("pass_top_delete_button")
            ) {
              Icon(
                imageVector = AppIcons.DeleteOutline,
                contentDescription = "Delete Card",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Main Physical Pass Header & Scannable Barcode Container
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f))
          ) {
            Column(
              modifier = Modifier
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      accentColor.copy(alpha = 0.25f),
                      Color.Transparent
                    )
                  )
                )
                .padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = accentColor.copy(alpha = 0.22f),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = CategoryIconMapper.getIcon(category?.iconName ?: "card"),
                      contentDescription = category?.name,
                      tint = accentColor,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = category?.name ?: "General",
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = card.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontSize = 27.sp,
                  lineHeight = 33.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
              )

              if (card.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = card.subtitle,
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center
                )
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Pure White High-Contrast Barcode Scanner Pad
              if (barcodeBitmap != null) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barcode_display_surface"),
                  color = Color.White,
                  shape = RoundedCornerShape(20.dp),
                  shadowElevation = 6.dp
                ) {
                  Column(
                    modifier = Modifier.padding(
                      horizontal = 20.dp,
                      vertical = if (formatType.is2D) 24.dp else 20.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Image(
                      bitmap = barcodeBitmap,
                      contentDescription = "Scannable ${formatType.displayName} for ${card.title}",
                      modifier = if (formatType.is2D) {
                        Modifier.size(210.dp)
                      } else {
                        Modifier
                          .fillMaxWidth()
                          .height(110.dp)
                      },
                      contentScale = ContentScale.FillBounds
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                      text = card.barcodeValue,
                      fontFamily = JetBrainsMonoFontFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = Color(0xFF0F172A),
                      letterSpacing = 1.5.sp,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              } else {
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Text(
                    text = "Photo / ID Card (No QR or Barcode attached)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                  )
                }
              }

              // Member / Card Number Copy Bar
              if (card.cardNumber.isNotBlank() || card.barcodeValue.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                val numberToCopy = card.cardNumber.ifBlank { card.barcodeValue }
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", numberToCopy))
                      Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("copy_card_number_bar"),
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "MEMBER / POLICY NUMBER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = numberToCopy,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                    Icon(
                      imageVector = AppIcons.ContentCopy,
                      contentDescription = "Copy Card Number",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Card Pictures Section (Front & Back Photos aligned side-by-side just like Edit View)
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = AppIcons.PhotoCamera,
                contentDescription = "Card Photos",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Pictures of Physical Card (Front & Back)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              PassPhotoSlot(
                label = "Front Photo",
                imagePath = card.frontImagePath,
                onViewPhoto = { path ->
                  fullscreenSide = "front"
                  fullscreenImagePath = path
                },
                onCropPhoto = { path ->
                  cropTargetSide = "front"
                  cropTargetPath = path
                },
                onAddPhotoClick = onEditClick,
                modifier = Modifier
                  .weight(1f)
                  .testTag("pass_front_photo_slot")
              )

              PassPhotoSlot(
                label = "Back Photo",
                imagePath = card.backImagePath,
                onViewPhoto = { path ->
                  fullscreenSide = "back"
                  fullscreenImagePath = path
                },
                onCropPhoto = { path ->
                  cropTargetSide = "back"
                  cropTargetPath = path
                },
                onAddPhotoClick = onEditClick,
                modifier = Modifier
                  .weight(1f)
                  .testTag("pass_back_photo_slot")
              )
            }
          }

          // Notes Section
          if (card.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceContainer,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(18.dp)
              ) {
                Text(
                  text = "NOTES & DETAILS",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = card.notes,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Bottom Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FilledTonalButton(
              onClick = onEditClick,
              modifier = Modifier
                .weight(1f)
                .testTag("bottom_edit_card_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit Card",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Edit Card / Photos")
            }

            OutlinedButton(
              onClick = { showDeleteConfirm = true },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
              modifier = Modifier.testTag("delete_card_button")
            ) {
              Icon(
                imageVector = AppIcons.DeleteOutline,
                contentDescription = "Delete Card",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Delete")
            }
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Fullscreen Image Viewer Dialog
  if (fullscreenImagePath != null) {
    Dialog(
      onDismissRequest = { fullscreenImagePath = null },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
          .clickable { fullscreenImagePath = null },
        contentAlignment = Alignment.Center
      ) {
        LocalCardImage(
          filePath = fullscreenImagePath!!,
          contentDescription = "Fullscreen Card Photo",
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        )
        Row(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 36.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilledTonalButton(
            onClick = {
              val path = fullscreenImagePath
              val side = fullscreenSide ?: "front"
              fullscreenImagePath = null
              if (path != null) {
                cropTargetSide = side
                cropTargetPath = path
              }
            },
            modifier = Modifier.testTag("fullscreen_crop_button")
          ) {
            Icon(
              imageVector = AppIcons.Crop,
              contentDescription = "Crop Photo",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Crop Photo")
          }
          OutlinedButton(
            onClick = { fullscreenImagePath = null }
          ) {
            Text("Close", color = Color.White)
          }
        }
      }
    }
  }

  if (cropTargetPath != null && cropTargetSide != null) {
    CardPhotoCropDialog(
      imagePath = cropTargetPath!!,
      sidePrefix = cropTargetSide!!,
      onDismiss = {
        cropTargetPath = null
        cropTargetSide = null
      },
      onCropSaved = { croppedPath ->
        val updated = if (cropTargetSide == "front") {
          card.copy(frontImagePath = croppedPath)
        } else {
          card.copy(backImagePath = croppedPath)
        }
        cropTargetPath = null
        cropTargetSide = null
        onUpdateCard(updated)
      }
    )
  }

  if (showDeleteConfirm) {
    DeleteCardConfirmDialog(
      card = card,
      onDismiss = { showDeleteConfirm = false },
      onConfirmDelete = {
        showDeleteConfirm = false
        onDeleteCard()
      }
    )
  }
}

@Composable
private fun PassPhotoSlot(
  label: String,
  imagePath: String?,
  onViewPhoto: (String) -> Unit,
  onCropPhoto: (String) -> Unit,
  onAddPhotoClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasPhoto = !imagePath.isNullOrBlank() && File(imagePath).exists()

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (hasPhoto) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onViewPhoto(imagePath!!) },
          contentAlignment = Alignment.Center
        ) {
          LocalCardImage(
            filePath = imagePath!!,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          // Crop icon badge on bottom-start
          Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(6.dp)
              .clickable { onCropPhoto(imagePath) }
              .testTag("pass_crop_${label.lowercase().replace(" ", "_")}_button")
          ) {
            Icon(
              imageVector = AppIcons.Crop,
              contentDescription = "Crop $label",
              tint = Color.White,
              modifier = Modifier
                .padding(5.dp)
                .size(14.dp)
            )
          }
          // Zoom icon badge on bottom-end
          Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
          ) {
            Icon(
              imageVector = AppIcons.ZoomIn,
              contentDescription = "Tap to Enlarge",
              tint = Color.White,
              modifier = Modifier
                .padding(5.dp)
                .size(14.dp)
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onAddPhotoClick),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No photo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
