package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.example.util.AppIcons
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.BarcodeFormatType
import com.example.util.ImageStorageHelper
import com.example.util.ScannedBarcodeResult
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerDialog(
  onDismiss: () -> Unit,
  onBarcodeScanned: (ScannedBarcodeResult) -> Unit
) {
  BackHandler { onDismiss() }

  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }
  var torchEnabled by remember { mutableStateOf(false) }
  var scanFeedbackMessage by remember { mutableStateOf<String?>(null) }
  var hasEmittedResult by remember { mutableStateOf(false) }
  var cameraControlRef by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasCameraPermission = granted
  }

  val photoScanLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      coroutineScope.launch {
        val scanned = ImageStorageHelper.scanBarcodeFromImageUri(context, uri)
        if (scanned != null) {
          hasEmittedResult = true
          onBarcodeScanned(scanned)
        } else {
          scanFeedbackMessage = "No QR or barcode found in selected photo. Try another image or enter code manually."
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
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
      Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
          val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
          val barcodeScanner = remember { BarcodeScanning.getClient() }

          DisposableEffect(Unit) {
            onDispose {
              cameraExecutor.shutdown()
              barcodeScanner.close()
            }
          }

          AndroidView(
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_preview_view"),
            factory = { ctx ->
              val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
              }
              val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
              cameraProviderFuture.addListener({
                try {
                  val cameraProvider = cameraProviderFuture.get()
                  val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                  }

                  val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                  imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !hasEmittedResult) {
                      val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                      )
                      barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                          val match = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                          if (match != null && !hasEmittedResult) {
                            hasEmittedResult = true
                            onBarcodeScanned(
                              ScannedBarcodeResult(
                                rawValue = match.rawValue!!.trim(),
                                formatType = BarcodeFormatType.fromMlKitFormat(match.format)
                              )
                            )
                          }
                        }
                        .addOnCompleteListener {
                          imageProxy.close()
                        }
                    } else {
                      imageProxy.close()
                    }
                  }

                  cameraProvider.unbindAll()
                  val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                  )
                  cameraControlRef = camera.cameraControl
                } catch (_: Exception) {
                }
              }, ContextCompat.getMainExecutor(ctx))
              previewView
            }
          )

          ScannerReticleOverlay()
        } else {
          // Camera permission denied or waiting state
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = AppIcons.QrCodeScanner,
              contentDescription = "Camera Required",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Camera Access for Live Scanning",
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Grant camera permission to point at any library, gym, or insurance barcode, or scan directly from a photo in your gallery.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
              onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
              modifier = Modifier.testTag("grant_camera_permission_button")
            ) {
              Text("Grant Camera Permission")
            }
          }
        }

        // Top Bar Controls
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .background(Color.Black.copy(alpha = 0.6f), CircleShape)
              .testTag("close_scanner_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close Scanner",
              tint = Color.White
            )
          }

          Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text(
              text = "Align QR or Barcode",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }

          IconButton(
            onClick = {
              val nextTorch = !torchEnabled
              torchEnabled = nextTorch
              cameraControlRef?.enableTorch(nextTorch)
            },
            modifier = Modifier
              .background(Color.Black.copy(alpha = 0.6f), CircleShape)
              .testTag("toggle_torch_button")
          ) {
            Icon(
              imageVector = if (torchEnabled) AppIcons.FlashOn else AppIcons.FlashOff,
              contentDescription = "Toggle Flashlight",
              tint = if (torchEnabled) MaterialTheme.colorScheme.primary else Color.White
            )
          }
        }

        // Bottom Controls Card
        Surface(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
          tonalElevation = 8.dp
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Supports QR Code, Code 128, Code 39, EAN-13, UPC-A, PDF417 & Codabar",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            if (scanFeedbackMessage != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = scanFeedbackMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              FilledTonalButton(
                onClick = {
                  scanFeedbackMessage = null
                  photoScanLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("scan_from_photo_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
              ) {
                Icon(
                  imageVector = AppIcons.PhotoLibrary,
                  contentDescription = "Scan from Photo",
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Photo", fontWeight = FontWeight.SemiBold)
              }

              OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                  .weight(1f)
                  .testTag("enter_code_manually_button")
              ) {
                Text("Type Code", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ScannerReticleOverlay() {
  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_y"
  )
  val primaryColor = MaterialTheme.colorScheme.primary

  Canvas(modifier = Modifier.fillMaxSize()) {
    val boxWidth = size.width * 0.78f
    val boxHeight = boxWidth * 0.65f
    val left = (size.width - boxWidth) / 2f
    val top = (size.height - boxHeight) / 2f - 40.dp.toPx()

    // Draw reticle frame
    drawRoundRect(
      color = primaryColor,
      topLeft = Offset(left, top),
      size = ComposeSize(boxWidth, boxHeight),
      cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
      style = Stroke(width = 3.dp.toPx())
    )

    // Draw animated laser line
    val laserY = top + (boxHeight * laserProgress)
    drawLine(
      color = primaryColor.copy(alpha = 0.85f),
      start = Offset(left + 16.dp.toPx(), laserY),
      end = Offset(left + boxWidth - 16.dp.toPx(), laserY),
      strokeWidth = 2.5.dp.toPx()
    )
  }
}
