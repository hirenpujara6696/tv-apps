package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TvShieldDarkColorScheme = darkColorScheme(
    primary = ShieldEmerald,
    onPrimary = ShieldEmeraldDark,
    primaryContainer = ShieldEmeraldContainer,
    onPrimaryContainer = ShieldEmerald,
    secondary = FocusCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = FocusCyanContainer,
    onSecondaryContainer = FocusCyan,
    tertiary = AmberWarn,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberWarnContainer,
    onTertiaryContainer = AmberWarn,
    error = AlertCoral,
    onError = ObsidianBg,
    errorContainer = AlertCoralContainer,
    onErrorContainer = AlertCoral,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceElevated,
    surfaceContainerHigh = SurfaceContainerHighest,
    outline = OutlineSubtle,
    outlineVariant = OutlineSubtle
)

private val TvShieldShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TvShieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvShieldDarkColorScheme,
        typography = AppTypography,
        shapes = TvShieldShapes,
        content = content
    )
}
