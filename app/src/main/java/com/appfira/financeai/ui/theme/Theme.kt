package com.appfira.financeai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.appfira.financeai.model.AppTheme

// New Color Palette: Light Blue
val LightBlue = Color(0xFF38BDF8)
val DeepBlue = Color(0xFF0284C7)
val Green = Color(0xFF10B981)
val Red = Color(0xFFEF4444)
val BackgroundLight = Color(0xFFF3F4F6) // Greyish White
val BackgroundDark = Color(0xFF000000) // AMOLED Black
val SurfaceLight = Color.White
val SurfaceDark = Color(0xFF1A1A1A) // Slightly lighter for contrast against black bg

// Glassmorphism Colors
val GlassBorderLight = Color(0xFFE5E7EB).copy(alpha = 0.5f)
val GlassBorderDark = Color(0xFF334155).copy(alpha = 0.4f)
val GlassSurfaceLight = Color.White.copy(alpha = 0.85f)
val GlassSurfaceDark = Color(0xFF1A1A1A).copy(alpha = 0.85f)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlue.copy(alpha = 0.2f),
    onPrimaryContainer = DeepBlue,
    secondary = LightBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    outline = GlassBorderLight,
    surfaceVariant = GlassSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = Color.Black,
    primaryContainer = DeepBlue.copy(alpha = 0.3f),
    onPrimaryContainer = LightBlue,
    secondary = DeepBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = GlassBorderDark,
    surfaceVariant = GlassSurfaceDark
)

val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * Mengembalikan BorderStroke untuk Card sesuai mode:
 * - Light mode → null (tidak ada border / shadow)
 * - Dark mode  → border tipis agar card terlihat di atas background hitam
 *
 * Gunakan sebagai: `border = cardBorder()` pada setiap Card.
 */
@Composable
fun cardBorder(
    darkWidth: Dp = 1.dp,
    darkColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
): BorderStroke? {
    val isDark = LocalDarkTheme.current
    return if (isDark) BorderStroke(darkWidth, darkColor) else null
}

/**
 * Versi dengan warna kustom untuk card yang diberi accent warna tertentu
 * (misal: card aktif spreadsheet). Di light mode tetap null.
 */
@Composable
fun cardBorderAccent(
    accentColor: Color,
    darkWidth: Dp = 1.2.dp,
    lightWidth: Dp = 1.2.dp
): BorderStroke? {
    val isDark = LocalDarkTheme.current
    return if (isDark)
        BorderStroke(darkWidth, accentColor.copy(alpha = 0.4f))
    else
        null
}


@Composable
fun FinanceAITheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = remember(theme, isSystemDark) {
        when (theme) {
            AppTheme.SYSTEM -> isSystemDark
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
        }
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            val activity = findActivity(context)
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Re-apply insets controller whenever darkTheme changes to ensure consistency
    LaunchedEffect(darkTheme) {
        val context = view.context
        val activity = findActivity(context)
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

private fun findActivity(context: android.content.Context): Activity? {
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
