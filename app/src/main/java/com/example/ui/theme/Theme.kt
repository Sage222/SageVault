package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SageVaultDarkColorScheme = darkColorScheme(
  primary = SagePrimary,
  onPrimary = SageOnPrimary,
  primaryContainer = SagePrimaryContainer,
  onPrimaryContainer = SageOnPrimaryContainer,
  secondary = MintSecondary,
  onSecondary = MintOnSecondary,
  secondaryContainer = MintSecondaryContainer,
  onSecondaryContainer = MintOnSecondaryContainer,
  tertiary = AmberTertiary,
  onTertiary = AmberOnTertiary,
  background = VaultBackground,
  onBackground = TextPrimary,
  surface = VaultSurface,
  onSurface = TextPrimary,
  surfaceVariant = VaultSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  surfaceContainer = VaultSurfaceElevated,
  surfaceContainerHigh = VaultSurfaceVariant,
  outline = VaultOutline,
  error = ErrorRed,
  onError = Color.Black
)

@Composable
fun SageVaultTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = false
          isAppearanceLightNavigationBars = false
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = SageVaultDarkColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  SageVaultTheme(content = content)
}
