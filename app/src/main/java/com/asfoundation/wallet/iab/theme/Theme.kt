package com.asfoundation.wallet.iab.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAppColors = staticCompositionLocalOf {
  lightColorPalette //default
}

private val LocalTypography = staticCompositionLocalOf {
  lightTypography //default
}

object IAPTheme {
  val colors: IAPColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

  val typography: IAPTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalTypography.current
}

@Composable
fun IAPTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colors = if (darkTheme) darkColorPalette else lightColorPalette
  val typography = if (darkTheme) darkTypography else lightTypography

  CompositionLocalProvider(
    LocalAppColors provides colors,
    LocalTypography provides typography,
  ) {
    MaterialTheme(
      colorScheme = colors.materialColors,
      typography = typography.typography,
      content = content
    )
  }
}
