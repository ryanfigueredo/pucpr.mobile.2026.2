package br.pucpr.cookbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainerLight,
    onPrimaryContainer = OnTerracottaLight,
    secondary = Olive,
    onSecondary = Color.White,
    secondaryContainer = OliveContainerLight,
    onSecondaryContainer = OnOliveLight,
    tertiary = Mustard,
    onTertiary = Color.White,
    tertiaryContainer = MustardContainerLight,
    onTertiaryContainer = OnMustardLight,
    background = CreamBackground,
    onBackground = OnCream,
    surface = CreamSurface,
    onSurface = OnCream,
    error = ErrorRed,
    errorContainer = ErrorRedContainer,
)

private val DarkColors = darkColorScheme(
    primary = TerracottaLight,
    onPrimary = OnTerracottaLight,
    primaryContainer = Terracotta,
    onPrimaryContainer = TerracottaContainerLight,
    secondary = OliveLight,
    onSecondary = OnOliveLight,
    secondaryContainer = Olive,
    onSecondaryContainer = OliveContainerLight,
    tertiary = MustardLight,
    onTertiary = OnMustardLight,
    tertiaryContainer = Mustard,
    onTertiaryContainer = MustardContainerLight,
    background = CoffeeDarkBackground,
    onBackground = OnCoffeeDark,
    surface = CoffeeDarkSurface,
    onSurface = OnCoffeeDark,
    error = ErrorRed,
    errorContainer = ErrorRedContainer,
)

@Composable
fun CookBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CookBookTypography,
        content = content,
    )
}
