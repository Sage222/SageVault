package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import com.example.util.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.BarcodeFormatType
import com.example.data.CardCategory
import com.example.data.PhysicalCard
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CardPhotoCropDialog
import com.example.ui.theme.CardPalette
import com.example.util.BarcodeGenerator
import com.example.util.CategoryIconMapper
import com.example.util.ImageStorageHelper
import com.example.util.LocalCardImage
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCardDialog(
  initialCard: PhysicalCard?,
  categories: List<CardCategory>,
  initialScannedCode: String? = null,
  initialScannedFormat: BarcodeFormatType? = null,
  onDismiss: () -> Unit,
  onSaveCard: (PhysicalCard) -> Unit,
  onOpenNewCategoryDialog: () -> Unit
) {
  BackHandler { onDismiss() }

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val defaultCategory = categories.firstOrNull()
  var title by remember { mutableStateOf(initialCard?.title ?: "") }
  var subtitle by remember { mutableStateOf(initialCard?.subtitle ?: "") }
  var cardNumber by remember {
    mutableStateOf(initialCard?.cardNumber ?: initialScannedCode ?: "")
  }
  var barcodeValue by remember {
    mutableStateOf(initialCard?.barcodeValue ?: initialScannedCode ?: "")
  }
  var selectedFormat by remember {
    mutableStateOf(
      initialScannedFormat
        ?: initialCard?.barcodeFormat?.let { BarcodeFormatType.fromName(it) }
        ?: BarcodeFormatType.CODE_128
    )
  }
  var selectedCategoryId by remember {
    mutableLongStateOf(initialCard?.categoryId ?: defaultCategory?.id ?: 1L)
  }
  var selectedColorHex by remember {
    mutableLongStateOf(
      initialCard?.colorHex ?: defaultCategory?.colorHex ?: CardPalette.SAGE_EMERALD
    )
  }
  var frontImagePath by remember { mutableStateOf(initialCard?.frontImagePath) }
  var backImagePath by remember { mutableStateOf(initialCard?.backImagePath) }
  var notes by remember { mutableStateOf(initialCard?.notes ?: "") }
  var isFavorite by remember { mutableStateOf(initialCard?.isFavorite ?: false) }

  var showScannerDialog by remember { mutableStateOf(false) }
  var pendingCameraSide by remember { mutableStateOf<String?>(null) }
  var pendingCameraTempFile by remember { mutableStateOf<File?>(null) }

  // Active photo being cropped ("front" or "back", plus file path)
  var cropTargetSide by remember { mutableStateOf<String?>(null) }
  var cropTargetPath by remember { mutableStateOf<String?>(null) }

  // Gallery Photo Pickers for Front and Back card photos
  val frontPhotoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        val savedPath = ImageStorageHelper.copyUriToInternalStorage(context, uri, "front")
        if (savedPath != null) {
          frontImagePath = savedPath
          cropTargetSide = "front"
          cropTargetPath = savedPath
        }
      }
    }
  }

  val backPhotoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        val savedPath = ImageStorageHelper.copyUriToInternalStorage(context, uri, "back")
        if (savedPath != null) {
          backImagePath = savedPath
          cropTargetSide = "back"
          cropTargetPath = savedPath
        }
      }
    }
  }

  // Camera capture launcher for Front and Back card photos
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success: Boolean ->
    val tempFile = pendingCameraTempFile
    val side = pendingCameraSide
    if (success && tempFile != null && side != null) {
      coroutineScope.launch {
        val savedPath = ImageStorageHelper.persistTempFile(context, tempFile, side)
        if (savedPath != null) {
          if (side == "front") frontImagePath = savedPath else backImagePath = savedPath
          cropTargetSide = side
          cropTargetPath = savedPath
        }
      }
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted && pendingCameraSide != null) {
      val (uri, tempFile) = ImageStorageHelper.createTempCameraUri(context)
      pendingCameraTempFile = tempFile
      takePictureLauncher.launch(uri)
    }
  }

  fun launchCameraForSide(side: String) {
    pendingCameraSide = side
    val hasPerm = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    if (hasPerm) {
      val (uri, tempFile) = ImageStorageHelper.createTempCameraUri(context)
      pendingCameraTempFile = tempFile
      takePictureLauncher.launch(uri)
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
        // Header bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_add_edit_card_button")
          ) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
          }

          Text(
            text = if (initialCard == null) "Add Physical Card" else "Edit Card",
            style = MaterialTheme.typography.titleLarge
          )

          Button(
            onClick = {
              if (title.isNotBlank()) {
                val cardToSave = (initialCard ?: PhysicalCard(title = title.trim())).copy(
                  title = title.trim(),
                  subtitle = subtitle.trim(),
                  cardNumber = cardNumber.trim().ifBlank { barcodeValue.trim() },
                  barcodeValue = barcodeValue.trim(),
                  barcodeFormat = selectedFormat.name,
                  categoryId = selectedCategoryId,
                  colorHex = selectedColorHex,
                  frontImagePath = frontImagePath,
                  backImagePath = backImagePath,
                  notes = notes.trim(),
                  isFavorite = isFavorite
                )
                onSaveCard(cardToSave)
              }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.testTag("save_card_button")
          ) {
            Text("Save")
          }
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
          // 1. Scan Barcode / QR Call-to-Action Banner
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Scan QR or Barcode",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Use camera or pick a photo to capture your card's code automatically.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Button(
                onClick = { showScannerDialog = true },
                modifier = Modifier.testTag("open_scanner_in_form_button")
              ) {
                Icon(
                  imageVector = AppIcons.QrCodeScanner,
                  contentDescription = "Scan",
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan")
              }
            }
          }

          // 2. Card Title
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Card Name (e.g., Central Library, Planet Fitness) *") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_title_input")
          )

          // 3. Category Selection + New Custom Category Button
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium
              )
              OutlinedButton(
                onClick = onOpenNewCategoryDialog,
                modifier = Modifier.testTag("add_custom_category_from_form_button")
              ) {
                Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = "New Category",
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Category")
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              categories.forEach { category ->
                val isSelected = selectedCategoryId == category.id
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedCategoryId = category.id
                    if (initialCard == null) {
                      selectedColorHex = category.colorHex
                    }
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
                  modifier = Modifier.testTag("select_category_chip_${category.id}")
                )
              }
            }
          }

          // 4. Barcode Value, Member Number & Format Selection
          OutlinedTextField(
            value = barcodeValue,
            onValueChange = {
              barcodeValue = it
              if (cardNumber.isBlank()) {
                cardNumber = it
              }
            },
            label = { Text("QR / Barcode Value (Scanned or Typed)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("barcode_value_input")
          )

          OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Displayed Member / Policy / Card ID") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_number_input")
          )

          // Live Barcode Preview if code is entered
          if (barcodeValue.isNotBlank() && selectedFormat != BarcodeFormatType.NONE) {
            val previewBitmap = remember(barcodeValue, selectedFormat) {
              BarcodeGenerator.generateBarcodeBitmap(barcodeValue, selectedFormat)
            }
            if (previewBitmap != null) {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Image(
                    bitmap = previewBitmap,
                    contentDescription = "Live Barcode Preview",
                    modifier = if (selectedFormat.is2D) {
                      Modifier.size(140.dp)
                    } else {
                      Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                    },
                    contentScale = ContentScale.FillBounds
                  )
                }
              }
            }
          }

          // 5. Card Pictures (Front & Back Photos)
          Column {
            Text(
              text = "Pictures of Physical Card (Front & Back)",
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Snap or pick photos of the front and back of your card, and crop them to fit.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              CardPhotoSlot(
                label = "Front Photo",
                imagePath = frontImagePath,
                onPickFromGallery = {
                  frontPhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                onTakePhoto = { launchCameraForSide("front") },
                onCropPhoto = {
                  if (!frontImagePath.isNullOrBlank()) {
                    cropTargetSide = "front"
                    cropTargetPath = frontImagePath
                  }
                },
                onRemovePhoto = { frontImagePath = null },
                modifier = Modifier
                  .weight(1f)
                  .testTag("front_photo_slot")
              )

              CardPhotoSlot(
                label = "Back Photo",
                imagePath = backImagePath,
                onPickFromGallery = {
                  backPhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                onTakePhoto = { launchCameraForSide("back") },
                onCropPhoto = {
                  if (!backImagePath.isNullOrBlank()) {
                    cropTargetSide = "back"
                    cropTargetPath = backImagePath
                  }
                },
                onRemovePhoto = { backImagePath = null },
                modifier = Modifier
                  .weight(1f)
                  .testTag("back_photo_slot")
              )
            }
          }

          // 6. Card Color Picker
          Column {
            Text(
              text = "Card Accent Color",
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              CardPalette.allColors.forEach { colorHex ->
                val isSelected = selectedColorHex == colorHex
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(colorHex))
                    .border(
                      width = if (isSelected) 3.dp else 1.dp,
                      color = if (isSelected) Color.White else Color.Transparent,
                      shape = CircleShape
                    )
                    .clickable { selectedColorHex = colorHex }
                    .testTag("color_swatch_$colorHex"),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = "Selected Color",
                      tint = Color.Black,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }

          // 7. Pin to Favorites Toggle
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { isFavorite = !isFavorite },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.Star,
                  contentDescription = "Favorite",
                  tint = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = "Pin to Favorites",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Switch(
                checked = isFavorite,
                onCheckedChange = { isFavorite = it },
                modifier = Modifier.testTag("favorite_switch_in_form")
              )
            }
          }

          // 8. Notes
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (e.g., copay, group ID, locker pin, hours)") },
            minLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_notes_input")
          )

          Spacer(modifier = Modifier.height(28.dp))
        }
      }
    }
  }

  if (showScannerDialog) {
    BarcodeScannerDialog(
      onDismiss = { showScannerDialog = false },
      onBarcodeScanned = { result ->
        barcodeValue = result.rawValue
        if (cardNumber.isBlank()) {
          cardNumber = result.rawValue
        }
        selectedFormat = result.formatType
        showScannerDialog = false
      }
    )
  }

  // Interactive Photo Crop Dialog
  if (cropTargetPath != null && cropTargetSide != null) {
    CardPhotoCropDialog(
      imagePath = cropTargetPath!!,
      sidePrefix = cropTargetSide!!,
      onDismiss = {
        cropTargetPath = null
        cropTargetSide = null
      },
      onCropSaved = { croppedPath ->
        if (cropTargetSide == "front") {
          frontImagePath = croppedPath
        } else {
          backImagePath = croppedPath
        }
        cropTargetPath = null
        cropTargetSide = null
      }
    )
  }
}

@Composable
private fun CardPhotoSlot(
  label: String,
  imagePath: String?,
  onPickFromGallery: () -> Unit,
  onTakePhoto: () -> Unit,
  onCropPhoto: () -> Unit,
  onRemovePhoto: () -> Unit,
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
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (hasPhoto) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCropPhoto)
        ) {
          LocalCardImage(
            filePath = imagePath!!,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          // Crop button overlay on top-left
          IconButton(
            onClick = onCropPhoto,
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(4.dp)
              .size(30.dp)
              .background(Color.Black.copy(alpha = 0.65f), CircleShape)
              .testTag("crop_${label.lowercase().replace(" ", "_")}_button")
          ) {
            Icon(
              imageVector = AppIcons.Crop,
              contentDescription = "Crop $label",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
          // Remove button overlay on top-right
          IconButton(
            onClick = onRemovePhoto,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(4.dp)
              .size(30.dp)
              .background(Color.Black.copy(alpha = 0.65f), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Remove $label",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
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
            .clickable(onClick = onPickFromGallery),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No photo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = onTakePhoto,
          modifier = Modifier.weight(1f),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
          Icon(
            imageVector = AppIcons.CameraAlt,
            contentDescription = "Camera",
            modifier = Modifier.size(16.dp)
          )
        }
        OutlinedButton(
          onClick = onPickFromGallery,
          modifier = Modifier.weight(1f),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
          Icon(
            imageVector = AppIcons.PhotoLibrary,
            contentDescription = "Gallery",
            modifier = Modifier.size(16.dp)
          )
        }
        if (hasPhoto) {
          OutlinedButton(
            onClick = onCropPhoto,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
          ) {
            Icon(
              imageVector = AppIcons.Crop,
              contentDescription = "Crop Photo",
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
