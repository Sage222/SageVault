package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core Obsidian & Sage Dark Theme Palette
val VaultBackground = Color(0xFF0A0F0D)
val VaultSurface = Color(0xFF121A16)
val VaultSurfaceElevated = Color(0xFF19241E)
val VaultSurfaceVariant = Color(0xFF213028)
val VaultOutline = Color(0xFF2D4237)

val SagePrimary = Color(0xFF4ADE80)
val SageOnPrimary = Color(0xFF052E16)
val SagePrimaryContainer = Color(0xFF14532D)
val SageOnPrimaryContainer = Color(0xFFBBF7D0)

val MintSecondary = Color(0xFF2DD4BF)
val MintOnSecondary = Color(0xFF042F2E)
val MintSecondaryContainer = Color(0xFF115E59)
val MintOnSecondaryContainer = Color(0xFF99F6E4)

val AmberTertiary = Color(0xFFFBBF24)
val AmberOnTertiary = Color(0xFF451A03)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val ErrorRed = Color(0xFFF87171)

// Preset Card & Category Accent Colors (Hex Longs for Room persistence)
object CardPalette {
  const val SAGE_EMERALD = 0xFF10B981L
  const val LIBRARY_INDIGO = 0xFF6366F1L
  const val GYM_CRIMSON = 0xFFF43F5EL
  const val INSURANCE_CYAN = 0xFF06B6D4L
  const val MEDICAL_TEAL = 0xFF14B8A6L
  const val RETAIL_AMBER = 0xFFF59E0BL
  const val TRANSIT_BLUE = 0xFF3B82F6L
  const val CLUB_VIOLET = 0xFF8B5CF6L
  const val SLATE_GRAPHITE = 0xFF475569L
  const val GOLD_VIP = 0xFFEAB308L
  const val SUNSET_ORANGE = 0xFFF97316L
  const val HOT_MAGENTA = 0xFFEC4899L
  const val LIME_ELECTRIC = 0xFF84CC16L
  const val SKY_AZURE = 0xFF0EA5E9L
  const val COPPER_BRONZE = 0xFFD97706L

  val allColors = listOf(
    SAGE_EMERALD,
    LIBRARY_INDIGO,
    GYM_CRIMSON,
    INSURANCE_CYAN,
    MEDICAL_TEAL,
    RETAIL_AMBER,
    TRANSIT_BLUE,
    CLUB_VIOLET,
    SLATE_GRAPHITE,
    GOLD_VIP,
    SUNSET_ORANGE,
    HOT_MAGENTA,
    LIME_ELECTRIC,
    SKY_AZURE,
    COPPER_BRONZE
  )
}
