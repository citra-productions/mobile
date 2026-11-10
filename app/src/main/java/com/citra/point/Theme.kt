package com.citra.point

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ---- Design tokens dari Figma (colors/primary/*) ----
val Navy = Color(0xFF1A3673)       // primary/500
val NavyDark = Color(0xFF0E214C)   // primary/700
val NavyDeep = Color(0xFF030B22)   // primary/900
val CitraRed = Color(0xFFD93A2B)
val WaGreen = Color(0xFF25D366)

private val Light = lightColorScheme(
    primary = Navy, onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EDF5), onPrimaryContainer = Color(0xFF304F85),
    background = Color.White, onBackground = NavyDeep,
    surface = Color.White, onSurface = NavyDeep,
    surfaceVariant = Color(0xFFF4F5F7), onSurfaceVariant = Color(0xFF525252), // gray-1
    outline = Color(0xFFD4D4D4),        // gray-2
    outlineVariant = Color(0xFFCDD4E3)  // primary/100
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9DB4FF), onPrimary = Color(0xFF0B1B4D),
    primaryContainer = Color(0xFF1F2B4D), onPrimaryContainer = Color(0xFFC7D4FF),
    background = Color(0xFF0F1320), onBackground = Color(0xFFE6E9F2),
    surface = Color(0xFF0F1320), onSurface = Color(0xFFE6E9F2),
    surfaceVariant = Color(0xFF1B2030), onSurfaceVariant = Color(0xFFA9B0C0),
    outline = Color(0xFF3A4157), outlineVariant = Color(0xFF2B3248)
)

val ColorScheme.isLight get() = background.luminance() > 0.5f
val ColorScheme.navInactive get() = if (isLight) Color(0xFF8791A3) else Color(0xFF8E97AB)
val ColorScheme.navBorder get() = if (isLight) Color(0xFFEBEDF2) else outlineVariant
val ColorScheme.priceColor get() = if (isLight) NavyDark else primary
val ColorScheme.pageBorder get() = if (isLight) Color(0xFFCED9FF) else outlineVariant
val ColorScheme.selectedOption get() = if (isLight) Color(0xFFE6EDFF) else primaryContainer

@Composable
fun CitraTheme(dark: Boolean, content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
