package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SereneLightColorScheme =
  lightColorScheme(
    primary = OasisPrimary,
    onPrimary = OasisOnPrimary,
    primaryContainer = OasisPrimaryContainer,
    onPrimaryContainer = OasisOnPrimaryContainer,
    inversePrimary = OasisInversePrimary,
    secondary = OasisSecondary,
    onSecondary = OasisOnSecondary,
    secondaryContainer = OasisSecondaryContainer,
    onSecondaryContainer = OasisOnSecondaryContainer,
    tertiary = OasisTertiary,
    onTertiary = OasisOnTertiary,
    tertiaryContainer = OasisTertiaryContainer,
    onTertiaryContainer = OasisOnTertiaryContainer,
    background = OasisSurface,
    onBackground = OasisOnSurface,
    surface = OasisSurface,
    onSurface = OasisOnSurface,
    surfaceVariant = OasisSurfaceContainerHighest,
    onSurfaceVariant = OasisOnSurfaceVariant,
    surfaceDim = OasisSurfaceDim,
    surfaceBright = OasisSurfaceBright,
    surfaceContainerLowest = OasisSurfaceContainerLowest,
    surfaceContainerLow = OasisSurfaceContainerLow,
    surfaceContainer = OasisSurfaceContainer,
    surfaceContainerHigh = OasisSurfaceContainerHigh,
    surfaceContainerHighest = OasisSurfaceContainerHighest,
    inverseSurface = OasisInverseSurface,
    inverseOnSurface = OasisInverseOnSurface,
    outline = OasisOutline,
    outlineVariant = OasisOutlineVariant,
    error = OasisError,
    onError = OasisOnError,
    errorContainer = OasisErrorContainer,
    onErrorContainer = OasisOnErrorContainer,
  )

private val SereneDarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = OasisTertiaryContainer,
    onTertiary = OasisOnTertiaryContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHigh,
    outline = OasisOutline,
    outlineVariant = OasisOnSurfaceVariant,
    error = OasisErrorContainer,
    onError = OasisOnErrorContainer,
  )

val SereneOasisShapes =
  Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun SereneOasisTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) SereneDarkColorScheme else SereneLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = SereneOasisTypography,
    shapes = SereneOasisShapes,
    content = content,
  )
}
