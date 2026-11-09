package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.AppIcons
import com.example.util.ImageStorageHelper
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class CropDragHandle {
  NONE,
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT,
  LEFT,
  TOP,
  RIGHT,
  BOTTOM,
  CENTER
}

@Composable
fun CardPhotoCropDialog(
  imagePath: String,
  sidePrefix: String,
  onDismiss: () -> Unit,
  onCropSaved: (String) -> Unit
) {
  BackHandler { onDismiss() }

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current

  var workingBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var isSaving by remember { mutableStateOf(false) }

  // Normalized crop coordinates [0f..1f]
  var cropLeft by remember { mutableFloatStateOf(0.06f) }
  var cropTop by remember { mutableFloatStateOf(0.06f) }
  var cropRight by remember { mutableFloatStateOf(0.94f) }
  var cropBottom by remember { mutableFloatStateOf(0.94f) }

  var canvasSize by remember { mutableStateOf(IntSize.Zero) }
  var activeHandle by remember { mutableStateOf(CropDragHandle.NONE) }

  val applyCardRatio: (Bitmap) -> Unit = { bmp ->
    val bmpW = bmp.width.toFloat().coerceAtLeast(1f)
    val bmpH = bmp.height.toFloat().coerceAtLeast(1f)
    val targetAspect = 1.586f // Standard ID-1 physical card ratio
    val currentAspect = bmpW / bmpH

    val margin = 0.06f
    val maxNormW = 1f - 2f * margin
    val maxNormH = 1f - 2f * margin

    val normW: Float
    val normH: Float
    if (maxNormW * currentAspect / maxNormH > targetAspect) {
      normH = maxNormH
      normW = (normH * targetAspect) / currentAspect
    } else {
      normW = maxNormW
      normH = (normW * currentAspect) / targetAspect
    }

    cropLeft = ((1f - normW) / 2f).coerceIn(0f, 0.9f)
    cropTop = ((1f - normH) / 2f).coerceIn(0f, 0.9f)
    cropRight = (cropLeft + normW).coerceIn(0.1f, 1f)
    cropBottom = (cropTop + normH).coerceIn(0.1f, 1f)
  }

  LaunchedEffect(imagePath) {
    isLoading = true
    val loaded = ImageStorageHelper.loadBitmapFromFile(imagePath, maxDimension = 1600)
    workingBitmap = loaded
    if (loaded != null) {
      applyCardRatio(loaded)
    }
    isLoading = false
  }

  val primaryColor = MaterialTheme.colorScheme.primary
  val padPx = with(density) { 24.dp.toPx() }
  val handleHitRadiusPx = with(density) { 42.dp.toPx() }
  val cornerBracketLenPx = with(density) { 22.dp.toPx() }
  val cornerStrokePx = with(density) { 4.dp.toPx() }
  val borderStrokePx = with(density) { 2.dp.toPx() }
  val gridStrokePx = with(density) { 1.dp.toPx() }

  Dialog(
    onDismissRequest = { if (!isSaving) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = Color(0xFF060A08)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
      ) {
        // Top Header Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            enabled = !isSaving,
            modifier = Modifier.testTag("cancel_crop_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Cancel Crop",
              tint = Color.White
            )
          }

          Text(
            text = "Crop Card Photo",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
          )

          Button(
            onClick = {
              val bmp = workingBitmap ?: return@Button
              isSaving = true
              coroutineScope.launch {
                val croppedPath = ImageStorageHelper.saveCroppedBitmap(
                  context = context,
                  sourceBitmap = bmp,
                  cropLeft = cropLeft,
                  cropTop = cropTop,
                  cropRight = cropRight,
                  cropBottom = cropBottom,
                  prefix = sidePrefix
                )
                isSaving = false
                if (croppedPath != null) {
                  onCropSaved(croppedPath)
                } else {
                  onDismiss()
                }
              }
            },
            enabled = workingBitmap != null && !isLoading && !isSaving,
            modifier = Modifier.testTag("apply_crop_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = "Apply Crop",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isSaving) "Saving..." else "Apply")
          }
        }

        Text(
          text = "Drag corners or edges to frame your physical card, or drag inside to move.",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.72f),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        )

        // Interactive Crop Canvas Area
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(12.dp)
            .onSizeChanged { canvasSize = it },
          contentAlignment = Alignment.Center
        ) {
          val bmp = workingBitmap
          if (isLoading) {
            CircularProgressIndicator(color = primaryColor)
          } else if (bmp == null) {
            Text(
              text = "Unable to load photo for cropping.",
              color = Color.White
            )
          } else {
            val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
            val containerW = canvasSize.width.toFloat().coerceAtLeast(1f)
            val containerH = canvasSize.height.toFloat().coerceAtLeast(1f)
            val availW = max(1f, containerW - 2f * padPx)
            val availH = max(1f, containerH - 2f * padPx)
            val scale = min(availW / bmp.width.toFloat(), availH / bmp.height.toFloat())
            val dispW = (bmp.width * scale).coerceAtLeast(1f)
            val dispH = (bmp.height * scale).coerceAtLeast(1f)
            val imgLeft = (containerW - dispW) / 2f
            val imgTop = (containerH - dispH) / 2f

            val currentImgLeft by rememberUpdatedState(imgLeft)
            val currentImgTop by rememberUpdatedState(imgTop)
            val currentDispW by rememberUpdatedState(dispW)
            val currentDispH by rememberUpdatedState(dispH)

            Canvas(
              modifier = Modifier
                .fillMaxSize()
                .testTag("crop_canvas")
                .pointerInput(bmp) {
                  detectDragGestures(
                    onDragStart = { startOffset ->
                      val rLeft = currentImgLeft + cropLeft * currentDispW
                      val rTop = currentImgTop + cropTop * currentDispH
                      val rRight = currentImgLeft + cropRight * currentDispW
                      val rBottom = currentImgTop + cropBottom * currentDispH

                      val distTL = hypot(startOffset.x - rLeft, startOffset.y - rTop)
                      val distTR = hypot(startOffset.x - rRight, startOffset.y - rTop)
                      val distBL = hypot(startOffset.x - rLeft, startOffset.y - rBottom)
                      val distBR = hypot(startOffset.x - rRight, startOffset.y - rBottom)

                      val minCornerDist = min(min(distTL, distTR), min(distBL, distBR))
                      activeHandle = when {
                        minCornerDist <= handleHitRadiusPx -> {
                          when (minCornerDist) {
                            distTL -> CropDragHandle.TOP_LEFT
                            distTR -> CropDragHandle.TOP_RIGHT
                            distBL -> CropDragHandle.BOTTOM_LEFT
                            else -> CropDragHandle.BOTTOM_RIGHT
                          }
                        }
                        abs(startOffset.x - rLeft) <= handleHitRadiusPx &&
                          startOffset.y in (rTop - handleHitRadiusPx)..(rBottom + handleHitRadiusPx) ->
                          CropDragHandle.LEFT
                        abs(startOffset.x - rRight) <= handleHitRadiusPx &&
                          startOffset.y in (rTop - handleHitRadiusPx)..(rBottom + handleHitRadiusPx) ->
                          CropDragHandle.RIGHT
                        abs(startOffset.y - rTop) <= handleHitRadiusPx &&
                          startOffset.x in (rLeft - handleHitRadiusPx)..(rRight + handleHitRadiusPx) ->
                          CropDragHandle.TOP
                        abs(startOffset.y - rBottom) <= handleHitRadiusPx &&
                          startOffset.x in (rLeft - handleHitRadiusPx)..(rRight + handleHitRadiusPx) ->
                          CropDragHandle.BOTTOM
                        startOffset.x in rLeft..rRight && startOffset.y in rTop..rBottom ->
                          CropDragHandle.CENTER
                        else -> CropDragHandle.NONE
                      }
                    },
                    onDragEnd = { activeHandle = CropDragHandle.NONE },
                    onDragCancel = { activeHandle = CropDragHandle.NONE },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      if (currentDispW <= 1f || currentDispH <= 1f) return@detectDragGestures
                      val dx = dragAmount.x / currentDispW
                      val dy = dragAmount.y / currentDispH
                      val minSpan = 0.12f

                      when (activeHandle) {
                        CropDragHandle.TOP_LEFT -> {
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        CropDragHandle.TOP_RIGHT -> {
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        CropDragHandle.BOTTOM_LEFT -> {
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        CropDragHandle.BOTTOM_RIGHT -> {
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        CropDragHandle.LEFT -> {
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                        }
                        CropDragHandle.RIGHT -> {
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                        }
                        CropDragHandle.TOP -> {
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        CropDragHandle.BOTTOM -> {
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        CropDragHandle.CENTER -> {
                          val width = cropRight - cropLeft
                          val height = cropBottom - cropTop
                          val newLeft = (cropLeft + dx).coerceIn(0f, 1f - width)
                          val newTop = (cropTop + dy).coerceIn(0f, 1f - height)
                          cropLeft = newLeft
                          cropRight = newLeft + width
                          cropTop = newTop
                          cropBottom = newTop + height
                        }
                        CropDragHandle.NONE -> {}
                      }
                    }
                  )
                }
            ) {
              // 1. Draw the card photo
              drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(imgLeft.roundToInt(), imgTop.roundToInt()),
                dstSize = IntSize(dispW.roundToInt(), dispH.roundToInt())
              )

              val rLeft = imgLeft + cropLeft * dispW
              val rTop = imgTop + cropTop * dispH
              val rRight = imgLeft + cropRight * dispW
              val rBottom = imgTop + cropBottom * dispH
              val rWidth = max(1f, rRight - rLeft)
              val rHeight = max(1f, rBottom - rTop)

              // 2. Dim outside areas (top, bottom, left, right)
              val scrimColor = Color.Black.copy(alpha = 0.65f)
              // Top scrim
              drawRect(
                color = scrimColor,
                topLeft = Offset(imgLeft, imgTop),
                size = Size(dispW, max(0f, rTop - imgTop))
              )
              // Bottom scrim
              drawRect(
                color = scrimColor,
                topLeft = Offset(imgLeft, rBottom),
                size = Size(dispW, max(0f, (imgTop + dispH) - rBottom))
              )
              // Left scrim
              drawRect(
                color = scrimColor,
                topLeft = Offset(imgLeft, rTop),
                size = Size(max(0f, rLeft - imgLeft), rHeight)
              )
              // Right scrim
              drawRect(
                color = scrimColor,
                topLeft = Offset(rRight, rTop),
                size = Size(max(0f, (imgLeft + dispW) - rRight), rHeight)
              )

              // 3. Draw rule-of-thirds grid inside crop rectangle
              val gridColor = Color.White.copy(alpha = 0.35f)
              val thirdW = rWidth / 3f
              val thirdH = rHeight / 3f
              drawLine(
                color = gridColor,
                start = Offset(rLeft + thirdW, rTop),
                end = Offset(rLeft + thirdW, rBottom),
                strokeWidth = gridStrokePx
              )
              drawLine(
                color = gridColor,
                start = Offset(rLeft + thirdW * 2f, rTop),
                end = Offset(rLeft + thirdW * 2f, rBottom),
                strokeWidth = gridStrokePx
              )
              drawLine(
                color = gridColor,
                start = Offset(rLeft, rTop + thirdH),
                end = Offset(rRight, rTop + thirdH),
                strokeWidth = gridStrokePx
              )
              drawLine(
                color = gridColor,
                start = Offset(rLeft, rTop + thirdH * 2f),
                end = Offset(rRight, rTop + thirdH * 2f),
                strokeWidth = gridStrokePx
              )

              // 4. Draw crop frame outline
              drawRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(rLeft, rTop),
                size = Size(rWidth, rHeight),
                style = Stroke(width = borderStrokePx)
              )

              // 5. Draw thick emerald corner handles
              val armW = min(cornerBracketLenPx, rWidth / 2f)
              val armH = min(cornerBracketLenPx, rHeight / 2f)

              // Top-Left
              drawLine(primaryColor, Offset(rLeft, rTop), Offset(rLeft + armW, rTop), cornerStrokePx, StrokeCap.Round)
              drawLine(primaryColor, Offset(rLeft, rTop), Offset(rLeft, rTop + armH), cornerStrokePx, StrokeCap.Round)

              // Top-Right
              drawLine(primaryColor, Offset(rRight, rTop), Offset(rRight - armW, rTop), cornerStrokePx, StrokeCap.Round)
              drawLine(primaryColor, Offset(rRight, rTop), Offset(rRight, rTop + armH), cornerStrokePx, StrokeCap.Round)

              // Bottom-Left
              drawLine(primaryColor, Offset(rLeft, rBottom), Offset(rLeft + armW, rBottom), cornerStrokePx, StrokeCap.Round)
              drawLine(primaryColor, Offset(rLeft, rBottom), Offset(rLeft, rBottom - armH), cornerStrokePx, StrokeCap.Round)

              // Bottom-Right
              drawLine(primaryColor, Offset(rRight, rBottom), Offset(rRight - armW, rBottom), cornerStrokePx, StrokeCap.Round)
              drawLine(primaryColor, Offset(rRight, rBottom), Offset(rRight, rBottom - armH), cornerStrokePx, StrokeCap.Round)
            }
          }
        }

        // Bottom Controls Bar (Rotate 90°, Card Ratio, Full Photo)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceContainer
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                val bmp = workingBitmap ?: return@OutlinedButton
                val rotated = ImageStorageHelper.rotateBitmap(bmp, 90f)
                workingBitmap = rotated
                applyCardRatio(rotated)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("crop_rotate_button")
            ) {
              Icon(
                imageVector = AppIcons.RotateRight,
                contentDescription = "Rotate 90 degrees",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Rotate")
            }

            OutlinedButton(
              onClick = {
                val bmp = workingBitmap ?: return@OutlinedButton
                applyCardRatio(bmp)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("crop_card_ratio_button")
            ) {
              Icon(
                imageVector = AppIcons.CreditCard,
                contentDescription = "Card Ratio",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Card Fit")
            }

            OutlinedButton(
              onClick = {
                cropLeft = 0f
                cropTop = 0f
                cropRight = 1f
                cropBottom = 1f
              },
              modifier = Modifier
                .weight(1f)
                .testTag("crop_full_photo_button")
            ) {
              Icon(
                imageVector = AppIcons.Crop,
                contentDescription = "Full Photo",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Full")
            }
          }
        }
      }
    }
  }
}
