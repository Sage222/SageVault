package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

  private fun vectorIcon(name: String, pathData: ImageVector.Builder.() -> Unit): ImageVector {
    return ImageVector.Builder(
      name = name,
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply(pathData).build()
  }

  private fun singlePathIcon(name: String, drawBlock: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector {
    return vectorIcon(name) {
      path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
        pathBuilder = drawBlock
      )
    }
  }

  val QrCodeScanner: ImageVector = singlePathIcon("QrCodeScanner") {
    // Corners
    moveTo(9.5f, 6.5f); verticalLineToRelative(3f); horizontalLineToRelative(-3f); verticalLineToRelative(-3f); horizontalLineToRelative(3f)
    moveTo(11f, 5f); horizontalLineTo(5f); verticalLineToRelative(6f); horizontalLineToRelative(6f); verticalLineTo(5f); close()
    moveTo(9.5f, 14.5f); verticalLineToRelative(3f); horizontalLineToRelative(-3f); verticalLineToRelative(-3f); horizontalLineToRelative(3f)
    moveTo(11f, 13f); horizontalLineTo(5f); verticalLineToRelative(6f); horizontalLineToRelative(6f); verticalLineToRelative(-6f); close()
    moveTo(17.5f, 6.5f); verticalLineToRelative(3f); horizontalLineToRelative(-3f); verticalLineToRelative(-3f); horizontalLineToRelative(3f)
    moveTo(19f, 5f); horizontalLineToRelative(-6f); verticalLineToRelative(6f); horizontalLineToRelative(6f); verticalLineTo(5f); close()
    // Inner QR dots & frame
    moveTo(13f, 13f); horizontalLineToRelative(1.5f); verticalLineToRelative(1.5f); horizontalLineTo(13f); close()
    moveTo(14.5f, 14.5f); horizontalLineTo(16f); verticalLineTo(16f); horizontalLineToRelative(-1.5f); close()
    moveTo(16f, 13f); horizontalLineToRelative(1.5f); verticalLineToRelative(1.5f); horizontalLineTo(16f); close()
    moveTo(13f, 16f); horizontalLineToRelative(1.5f); verticalLineToRelative(1.5f); horizontalLineTo(13f); close()
    moveTo(14.5f, 17.5f); horizontalLineTo(16f); verticalLineTo(19f); horizontalLineToRelative(-1.5f); close()
    moveTo(16f, 16f); horizontalLineToRelative(1.5f); verticalLineToRelative(1.5f); horizontalLineTo(16f); close()
    moveTo(17.5f, 14.5f); horizontalLineTo(19f); verticalLineTo(16f); horizontalLineToRelative(-1.5f); close()
    moveTo(17.5f, 17.5f); horizontalLineTo(19f); verticalLineTo(19f); horizontalLineToRelative(-1.5f); close()
    moveTo(22f, 7f); horizontalLineToRelative(-2f); verticalLineTo(4f); horizontalLineToRelative(-3f); verticalLineTo(2f); horizontalLineToRelative(5f); verticalLineToRelative(5f); close()
    moveTo(22f, 22f); verticalLineToRelative(-5f); horizontalLineToRelative(-2f); verticalLineToRelative(3f); horizontalLineToRelative(-3f); verticalLineToRelative(2f); horizontalLineToRelative(5f); close()
    moveTo(2f, 22f); horizontalLineToRelative(5f); verticalLineToRelative(-2f); horizontalLineTo(4f); verticalLineToRelative(-3f); horizontalLineTo(2f); verticalLineToRelative(5f); close()
    moveTo(2f, 2f); verticalLineToRelative(5f); horizontalLineToRelative(2f); verticalLineTo(4f); horizontalLineToRelative(3f); verticalLineTo(2f); horizontalLineTo(2f); close()
  }

  val QrCode2: ImageVector = QrCodeScanner

  val FlashOn: ImageVector = singlePathIcon("FlashOn") {
    moveTo(7f, 2f); verticalLineToRelative(11f); horizontalLineToRelative(3f); verticalLineToRelative(9f); lineToRelative(7f, -12f); horizontalLineToRelative(-4f); lineToRelative(4f, -8f); close()
  }

  val FlashOff: ImageVector = singlePathIcon("FlashOff") {
    moveTo(3.27f, 3f); lineTo(2f, 4.27f); lineToRelative(5f, 5f); verticalLineTo(13f); horizontalLineToRelative(3f); verticalLineToRelative(9f); lineToRelative(3.58f, -6.14f); lineTo(17.73f, 20f); lineTo(19f, 18.73f); lineTo(3.27f, 3f); close()
    moveTo(17f, 10f); horizontalLineToRelative(-4f); lineToRelative(4f, -8f); horizontalLineTo(7f); verticalLineToRelative(2.18f); lineToRelative(8.46f, 8.46f); lineTo(17f, 10f); close()
  }

  val PhotoLibrary: ImageVector = singlePathIcon("PhotoLibrary") {
    moveTo(22f, 16f); verticalLineTo(4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); horizontalLineTo(8f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); close()
    moveTo(11f, 12f); lineToRelative(2.03f, 2.71f); lineTo(16f, 11f); lineToRelative(4f, 5f); horizontalLineTo(8f); lineToRelative(3f, -4f); close()
    moveTo(2f, 6f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(14f); verticalLineToRelative(-2f); horizontalLineTo(4f); verticalLineTo(6f); horizontalLineTo(2f); close()
  }

  val PhotoCamera: ImageVector = singlePathIcon("PhotoCamera") {
    moveTo(12f, 12f); moveToRelative(-3.2f, 0f)
    arcToRelative(3.2f, 3.2f, 0f, true, true, 6.4f, 0f)
    arcToRelative(3.2f, 3.2f, 0f, true, true, -6.4f, 0f)
    moveTo(9f, 2f); lineTo(7.17f, 4f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(6f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); horizontalLineToRelative(-3.17f); lineTo(15f, 2f); horizontalLineTo(9f); close()
    moveTo(12f, 17f); curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f); reflectiveCurveToRelative(2.24f, -5f, 5f, -5f); reflectiveCurveToRelative(5f, 2.24f, 5f, 5f); reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f); close()
  }

  val CameraAlt: ImageVector = PhotoCamera
  val FlipCameraAndroid: ImageVector = PhotoCamera

  val ZoomIn: ImageVector = singlePathIcon("ZoomIn") {
    moveTo(15.5f, 14f); horizontalLineToRelative(-0.79f); lineToRelative(-0.28f, -0.27f); curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f); curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f); reflectiveCurveTo(3f, 5.91f, 3f, 9.5f); reflectiveCurveTo(5.91f, 16f, 9.5f, 16f); curveToRelative(1.61f, 0f, 3.09f, -0.59f, 4.23f, -1.57f); lineToRelative(0.27f, 0.28f); verticalLineToRelative(0.79f); lineToRelative(5f, 4.99f); lineTo(20.49f, 19f); lineToRelative(-4.99f, -5f); close()
    moveTo(9.5f, 14f); curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f); reflectiveCurveTo(7.01f, 5f, 9.5f, 5f); reflectiveCurveTo(14f, 7.01f, 14f, 9.5f); reflectiveCurveTo(11.99f, 14f, 9.5f, 14f); close()
    moveTo(12f, 10f); horizontalLineToRelative(-2f); verticalLineToRelative(2f); horizontalLineTo(9f); verticalLineToRelative(-2f); horizontalLineTo(7f); verticalLineTo(9f); horizontalLineToRelative(2f); verticalLineTo(7f); horizontalLineToRelative(1f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineToRelative(1f); close()
  }

  val Crop: ImageVector = singlePathIcon("Crop") {
    moveTo(17f, 15f); horizontalLineToRelative(2f); verticalLineTo(7f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); horizontalLineTo(9f); verticalLineToRelative(2f); horizontalLineToRelative(8f); verticalLineToRelative(8f); close()
    moveTo(7f, 17f); verticalLineTo(1f); horizontalLineTo(5f); verticalLineToRelative(4f); horizontalLineTo(1f); verticalLineToRelative(2f); horizontalLineToRelative(4f); verticalLineToRelative(10f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(10f); verticalLineToRelative(4f); horizontalLineToRelative(2f); verticalLineToRelative(-4f); horizontalLineToRelative(4f); verticalLineToRelative(-2f); horizontalLineTo(7f); close()
  }

  val RotateRight: ImageVector = singlePathIcon("RotateRight") {
    moveTo(15.55f, 5.55f); lineTo(11f, 1f); verticalLineToRelative(3.07f); curveTo(7.06f, 4.56f, 4f, 7.92f, 4f, 12f); reflectiveCurveToRelative(3.05f, 7.44f, 7f, 7.93f); verticalLineToRelative(-2.02f); curveToRelative(-2.84f, -0.48f, -5f, -2.94f, -5f, -5.91f); reflectiveCurveToRelative(2.16f, -5.43f, 5f, -5.91f); verticalLineTo(10f); lineToRelative(4.55f, -4.45f); close()
    moveTo(19.93f, 11f); curveToRelative(-0.17f, -1.39f, -0.72f, -2.73f, -1.62f, -3.89f); lineToRelative(-1.42f, 1.42f); curveToRelative(0.54f, 0.75f, 0.88f, 1.6f, 1.02f, 2.47f); horizontalLineToRelative(2.02f); close()
    moveTo(13f, 17.9f); verticalLineToRelative(2.02f); curveToRelative(1.39f, -0.17f, 2.74f, -0.71f, 3.9f, -1.61f); lineToRelative(-1.44f, -1.44f); curveToRelative(-0.75f, 0.54f, -1.59f, 0.89f, -2.46f, 1.03f); close()
    moveTo(16.89f, 15.48f); lineToRelative(1.42f, 1.41f); curveToRelative(0.9f, -1.16f, 1.45f, -2.5f, 1.62f, -3.89f); horizontalLineToRelative(-2.02f); curveToRelative(-0.14f, 0.87f, -0.48f, 1.72f, -1.02f, 2.48f); close()
  }

  val DriveFileRenameOutline: ImageVector = singlePathIcon("DriveFileRenameOutline") {
    moveTo(15f, 16f); lineToRelative(-4f, 4f); horizontalLineToRelative(10f); verticalLineToRelative(-4f); horizontalLineToRelative(-6f); close()
    moveTo(12.06f, 7.19f); lineTo(3f, 16.25f); verticalLineTo(20f); horizontalLineToRelative(3.75f); lineToRelative(9.06f, -9.06f); lineToRelative(-3.75f, -3.75f); close()
    moveTo(18.71f, 8.04f); curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f); lineToRelative(-2.34f, -2.34f); curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f); lineToRelative(-1.83f, 1.83f); lineToRelative(3.75f, 3.75f); lineToRelative(1.83f, -1.83f); close()
  }

  val BrightnessHigh: ImageVector = singlePathIcon("BrightnessHigh") {
    moveTo(20f, 8.69f); verticalLineTo(4f); horizontalLineToRelative(-4.69f); lineTo(12f, 0.69f); lineTo(8.69f, 4f); horizontalLineTo(4f); verticalLineToRelative(4.69f); lineTo(0.69f, 12f); lineTo(4f, 15.31f); verticalLineTo(20f); horizontalLineToRelative(4.69f); lineTo(12f, 23.31f); lineTo(15.31f, 20f); horizontalLineTo(20f); verticalLineToRelative(-4.69f); lineTo(23.31f, 12f); lineTo(20f, 8.69f); close()
    moveTo(12f, 18f); curveToRelative(-3.31f, 0f, -6f, -2.69f, -6f, -6f); reflectiveCurveToRelative(2.69f, -6f, 6f, -6f); reflectiveCurveToRelative(6f, 2.69f, 6f, 6f); reflectiveCurveToRelative(-2.69f, 6f, -6f, 6f); close()
  }

  val ContentCopy: ImageVector = singlePathIcon("ContentCopy") {
    moveTo(16f, 1f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); horizontalLineToRelative(2f); verticalLineTo(3f); horizontalLineToRelative(12f); verticalLineTo(1f); close()
    moveTo(19f, 5f); horizontalLineTo(8f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(11f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(7f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
    moveTo(19f, 21f); horizontalLineTo(8f); verticalLineTo(7f); horizontalLineToRelative(11f); verticalLineToRelative(14f); close()
  }

  val DeleteOutline: ImageVector = singlePathIcon("DeleteOutline") {
    moveTo(6f, 19f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(8f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(7f); horizontalLineTo(6f); verticalLineToRelative(12f); close()
    moveTo(8f, 9f); horizontalLineToRelative(8f); verticalLineToRelative(10f); horizontalLineTo(8f); verticalLineTo(9f); close()
    moveTo(15.5f, 4f); lineToRelative(-1f, -1f); horizontalLineToRelative(-5f); lineToRelative(-1f, 1f); horizontalLineTo(5f); verticalLineToRelative(2f); horizontalLineToRelative(14f); verticalLineTo(4f); close()
  }

  val StarBorder: ImageVector = singlePathIcon("StarBorder") {
    moveTo(22f, 9.24f); lineToRelative(-7.19f, -0.62f); lineTo(12f, 2f); lineTo(9.19f, 8.63f); lineTo(2f, 9.24f); lineToRelative(5.46f, 4.73f); lineTo(5.82f, 21f); lineTo(12f, 17.27f); lineTo(18.18f, 21f); lineToRelative(-1.63f, -7.03f); lineTo(22f, 9.24f); close()
    moveTo(12f, 15.4f); lineToRelative(-3.76f, 2.27f); lineToRelative(1f, -4.28f); lineToRelative(-3.32f, -2.88f); lineToRelative(4.38f, -0.38f); lineTo(12f, 6.1f); lineToRelative(1.71f, 4.04f); lineToRelative(4.38f, 0.38f); lineToRelative(-3.32f, 2.88f); lineToRelative(1f, 4.28f); lineTo(12f, 15.4f); close()
  }

  val ArrowUpward: ImageVector = singlePathIcon("ArrowUpward") {
    moveTo(4f, 12f); lineToRelative(1.41f, 1.41f); lineTo(11f, 7.83f); verticalLineTo(20f); horizontalLineToRelative(2f); verticalLineTo(7.83f); lineToRelative(5.58f, 5.59f); lineTo(20f, 12f); lineToRelative(-8f, -8f); lineToRelative(-8f, 8f); close()
  }

  val ArrowDownward: ImageVector = singlePathIcon("ArrowDownward") {
    moveTo(20f, 12f); lineToRelative(-1.41f, -1.41f); lineTo(13f, 16.17f); verticalLineTo(4f); horizontalLineToRelative(-2f); verticalLineToRelative(12.17f); lineToRelative(-5.58f, -5.59f); lineTo(4f, 12f); lineToRelative(8f, 8f); lineToRelative(8f, -8f); close()
  }

  val VerticalAlignTop: ImageVector = singlePathIcon("VerticalAlignTop") {
    moveTo(8f, 11f); horizontalLineToRelative(3f); verticalLineToRelative(10f); horizontalLineToRelative(2f); verticalLineTo(11f); horizontalLineToRelative(3f); lineToRelative(-4f, -4f); lineToRelative(-4f, 4f); close()
    moveTo(4f, 3f); verticalLineToRelative(2f); horizontalLineToRelative(16f); verticalLineTo(3f); horizontalLineTo(4f); close()
  }

  val SwapVert: ImageVector = singlePathIcon("SwapVert") {
    moveTo(16f, 17.01f); verticalLineTo(10f); horizontalLineToRelative(-2f); verticalLineToRelative(7.01f); horizontalLineToRelative(-3f); lineTo(15f, 21f); lineToRelative(4f, -3.99f); horizontalLineToRelative(-3f); close()
    moveTo(9f, 3f); lineTo(5f, 6.99f); horizontalLineToRelative(3f); verticalLineTo(14f); horizontalLineToRelative(2f); verticalLineTo(6.99f); horizontalLineToRelative(3f); lineTo(9f, 3f); close()
  }

  val CreditCard: ImageVector = singlePathIcon("CreditCard") {
    moveTo(20f, 4f); horizontalLineTo(4f); curveToRelative(-1.11f, 0f, -1.99f, 0.89f, -1.99f, 2f); lineTo(2f, 18f); curveToRelative(0f, 1.11f, 0.89f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.11f, 0f, 2f, -0.89f, 2f, -2f); verticalLineTo(6f); curveToRelative(0f, -1.11f, -0.89f, -2f, -2f, -2f); close()
    moveTo(20f, 18f); horizontalLineTo(4f); verticalLineToRelative(-6f); horizontalLineToRelative(16f); verticalLineToRelative(6f); close()
    moveTo(20f, 8f); horizontalLineTo(4f); verticalLineTo(6f); horizontalLineToRelative(16f); verticalLineToRelative(2f); close()
  }

  val AddCard: ImageVector = singlePathIcon("AddCard") {
    moveTo(20f, 4f); horizontalLineTo(4f); curveToRelative(-1.11f, 0f, -1.99f, 0.89f, -1.99f, 2f); lineTo(2f, 18f); curveToRelative(0f, 1.11f, 0.89f, 2f, 2f, 2f); horizontalLineToRelative(10f); verticalLineToRelative(-2f); horizontalLineTo(4f); verticalLineToRelative(-6f); horizontalLineToRelative(18f); verticalLineTo(6f); curveToRelative(0f, -1.11f, -0.89f, -2f, -2f, -2f); close()
    moveTo(20f, 8f); horizontalLineTo(4f); verticalLineTo(6f); horizontalLineToRelative(16f); verticalLineToRelative(2f); close()
    moveTo(24f, 17f); verticalLineToRelative(2f); horizontalLineToRelative(-3f); verticalLineToRelative(3f); horizontalLineToRelative(-2f); verticalLineToRelative(-3f); horizontalLineToRelative(-3f); verticalLineToRelative(-2f); horizontalLineToRelative(3f); verticalLineToRelative(-3f); horizontalLineToRelative(2f); verticalLineToRelative(3f); horizontalLineToRelative(3f); close()
  }

  val Category: ImageVector = singlePathIcon("Category") {
    moveTo(12f, 2f); lineToRelative(-5.5f, 9f); horizontalLineToRelative(11f); close()
    moveTo(17.5f, 17.5f); moveToRelative(-4.5f, 0f)
    arcToRelative(4.5f, 4.5f, 0f, true, true, 9f, 0f)
    arcToRelative(4.5f, 4.5f, 0f, true, true, -9f, 0f)
    moveTo(3f, 13.5f); horizontalLineToRelative(8f); verticalLineToRelative(8f); horizontalLineTo(3f); close()
  }

  // Category Icons
  val LocalLibrary: ImageVector = singlePathIcon("LocalLibrary") {
    moveTo(12f, 11.55f); curveTo(9.64f, 9.35f, 6.48f, 8f, 3f, 8f); verticalLineToRelative(11f); curveToRelative(3.48f, 0f, 6.64f, 1.35f, 9f, 3.55f); curveToRelative(2.36f, -2.19f, 5.52f, -3.55f, 9f, -3.55f); verticalLineTo(8f); curveToRelative(-3.48f, 0f, -6.64f, 1.35f, -9f, 3.55f); close()
    moveTo(12f, 8f); curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f); reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f); reflectiveCurveToRelative(-3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); close()
  }

  val FitnessCenter: ImageVector = singlePathIcon("FitnessCenter") {
    moveTo(20.57f, 14.86f); lineTo(22f, 13.43f); lineTo(20.57f, 12f); lineTo(17f, 15.57f); lineTo(8.43f, 7f); lineTo(12f, 3.43f); lineTo(10.57f, 2f); lineTo(9.14f, 3.43f); lineTo(7.71f, 2f); lineTo(5.57f, 4.14f); lineTo(4.14f, 2.71f); lineTo(2.71f, 4.14f); lineToRelative(1.43f, 1.43f); lineTo(2f, 7.71f); lineToRelative(1.43f, 1.43f); lineTo(2f, 10.57f); lineTo(3.43f, 12f); lineTo(7f, 8.43f); lineTo(15.57f, 17f); lineTo(12f, 20.57f); lineTo(13.43f, 22f); lineToRelative(1.43f, -1.43f); lineTo(16.29f, 22f); lineToRelative(2.14f, -2.14f); lineToRelative(1.43f, 1.43f); lineToRelative(1.43f, -1.43f); lineToRelative(-1.43f, -1.43f); lineTo(22f, 16.29f); close()
  }

  val HealthAndSafety: ImageVector = singlePathIcon("HealthAndSafety") {
    moveTo(10.5f, 13f); horizontalLineTo(8f); verticalLineToRelative(-3f); horizontalLineToRelative(2.5f); verticalLineTo(7.5f); horizontalLineToRelative(3f); verticalLineTo(10f); horizontalLineTo(16f); verticalLineToRelative(3f); horizontalLineToRelative(-2.5f); verticalLineToRelative(2.5f); horizontalLineToRelative(-3f); verticalLineTo(13f); close()
    moveTo(12f, 2f); lineTo(4f, 5f); verticalLineToRelative(6.09f); curveToRelative(0f, 5.05f, 3.41f, 9.76f, 8f, 10.91f); curveToRelative(4.59f, -1.15f, 8f, -5.86f, 8f, -10.91f); verticalLineTo(5f); lineTo(12f, 2f); close()
  }

  val LocalHospital: ImageVector = singlePathIcon("LocalHospital") {
    moveTo(19f, 3f); horizontalLineTo(5f); curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(3f, 19f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(5f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
    moveTo(18f, 14f); horizontalLineToRelative(-4f); verticalLineToRelative(4f); horizontalLineToRelative(-4f); verticalLineToRelative(-4f); horizontalLineTo(6f); verticalLineToRelative(-4f); horizontalLineToRelative(4f); verticalLineTo(6f); horizontalLineToRelative(4f); verticalLineToRelative(4f); horizontalLineToRelative(4f); verticalLineToRelative(4f); close()
  }

  val WorkspacePremium: ImageVector = singlePathIcon("WorkspacePremium") {
    moveTo(12f, 4f); curveToRelative(-3.31f, 0f, -6f, 2.69f, -6f, 6f); curveToRelative(0f, 2.22f, 1.21f, 4.15f, 3f, 5.19f); verticalLineTo(22f); lineToRelative(3f, -2f); lineToRelative(3f, 2f); verticalLineToRelative(-6.81f); curveToRelative(1.79f, -1.04f, 3f, -2.97f, 3f, -5.19f); curveToRelative(0f, -3.31f, -2.69f, -6f, -6f, -6f); close()
  }

  val ShoppingBag: ImageVector = singlePathIcon("ShoppingBag") {
    moveTo(18f, 6f); horizontalLineToRelative(-2f); curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f); reflectiveCurveTo(8f, 3.79f, 8f, 6f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
    moveTo(12f, 4f); curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f); horizontalLineToRelative(-4f); curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f); close()
  }

  val DirectionsBus: ImageVector = singlePathIcon("DirectionsBus") {
    moveTo(4f, 16f); curveToRelative(0f, 0.88f, 0.39f, 1.67f, 1f, 2.22f); verticalLineTo(20f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); horizontalLineToRelative(1f); curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f); verticalLineToRelative(-1f); horizontalLineToRelative(8f); verticalLineToRelative(1f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); horizontalLineToRelative(1f); curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f); verticalLineToRelative(-1.78f); curveToRelative(0.61f, -0.55f, 1f, -1.34f, 1f, -2.22f); verticalLineTo(6f); curveToRelative(0f, -3.5f, -3.58f, -4f, -8f, -4f); reflectiveCurveToRelative(-8f, 0.5f, -8f, 4f); verticalLineToRelative(10f); close()
    moveTo(18f, 11f); horizontalLineTo(6f); verticalLineTo(6f); horizontalLineToRelative(12f); verticalLineToRelative(5f); close()
  }

  val Badge: ImageVector = singlePathIcon("Badge") {
    moveTo(20f, 7f); horizontalLineToRelative(-5f); verticalLineTo(4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); horizontalLineToRelative(-2f); curveTo(9.9f, 2f, 9f, 2.9f, 9f, 4f); verticalLineToRelative(3f); horizontalLineTo(4f); curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f); verticalLineToRelative(11f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(9f); curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f); close()
    moveTo(11f, 4f); horizontalLineToRelative(2f); verticalLineToRelative(5f); horizontalLineToRelative(-2f); verticalLineTo(4f); close()
  }

  val ConfirmationNumber: ImageVector = singlePathIcon("ConfirmationNumber") {
    moveTo(22f, 10f); verticalLineTo(6f); curveToRelative(0f, -1.11f, -0.9f, -2f, -2f, -2f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -1.99f, 0.89f, -1.99f, 2f); verticalLineToRelative(4f); curveToRelative(1.1f, 0f, 1.99f, 0.9f, 1.99f, 2f); reflectiveCurveToRelative(-0.89f, 2f, -2f, 2f); verticalLineToRelative(4f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineToRelative(-4f); curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f); reflectiveCurveToRelative(0.9f, -2f, 2f, -2f); close()
  }

  val AccountBalance: ImageVector = singlePathIcon("AccountBalance") {
    moveTo(4f, 10f); verticalLineToRelative(7f); horizontalLineToRelative(3f); verticalLineToRelative(-7f); horizontalLineTo(4f); close()
    moveTo(10f, 10f); verticalLineToRelative(7f); horizontalLineToRelative(3f); verticalLineToRelative(-7f); horizontalLineToRelative(-3f); close()
    moveTo(2f, 22f); horizontalLineToRelative(19f); verticalLineToRelative(-3f); horizontalLineTo(2f); verticalLineToRelative(3f); close()
    moveTo(16f, 10f); verticalLineToRelative(7f); horizontalLineToRelative(3f); verticalLineToRelative(-7f); horizontalLineToRelative(-3f); close()
    moveTo(11.5f, 1f); lineTo(2f, 6f); verticalLineToRelative(2f); horizontalLineToRelative(19f); verticalLineTo(6f); lineToRelative(-9.5f, -5f); close()
  }

  val FolderSpecial: ImageVector = singlePathIcon("FolderSpecial") {
    moveTo(20f, 6f); horizontalLineToRelative(-8f); lineToRelative(-2f, -2f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
    moveTo(17.94f, 17f); lineTo(15f, 15.28f); lineTo(12.06f, 17f); lineToRelative(0.78f, -3.33f); lineToRelative(-2.59f, -2.24f); lineToRelative(3.41f, -0.29f); lineTo(15f, 8f); lineToRelative(1.34f, 3.14f); lineToRelative(3.41f, 0.29f); lineToRelative(-2.59f, 2.24f); lineToRelative(0.78f, 3.33f); close()
  }
}
