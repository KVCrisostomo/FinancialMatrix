package com.karlvcrisostomo.financialmatrix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PremiumGold,
    secondary = LightGold,
    tertiary = SuccessGreen,
    background = MidnightNavy,
    surface = MidnightNavy,
    onPrimary = MidnightNavy,
    onSecondary = MidnightNavy,
    onTertiary = MidnightNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = PremiumGold.copy(alpha = 0.2f),
    onPrimaryContainer = PremiumGold,
    secondaryContainer = LightGold.copy(alpha = 0.2f),
    onSecondaryContainer = LightGold,
    tertiaryContainer = SuccessGreen.copy(alpha = 0.2f),
    onTertiaryContainer = SuccessGreen,
    surfaceVariant = Color.White.copy(alpha = 0.1f),
    onSurfaceVariant = Color.White,
    error = AlertRed,
    onError = MidnightNavy,
    errorContainer = AlertRed.copy(alpha = 0.2f),
    onErrorContainer = AlertRed
)

private val LightColorScheme = DarkColorScheme // Force convergence

@Composable
fun FinancialMatrixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled in Phase 5.1.1 for brand convergence
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
