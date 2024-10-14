package com.appcoins.wallet.ui.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

val lightAndroidBackgroundTheme = BackgroundTheme(color = styleguide_white)
val darkAndroidBackgroundTheme = BackgroundTheme(color = styleguide_blue)

/**
 * App theme
 * @param darkTheme Whether the theme should use a dark color scheme (is dark by default).
 */
@Composable
fun WalletTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) darkWalletColorScheme else lightWalletColorScheme
  val backgroundTheme = if (darkTheme) darkAndroidBackgroundTheme else lightAndroidBackgroundTheme

  CompositionLocalProvider(
    LocalBackgroundTheme provides backgroundTheme,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      shapes = shapes,
      content = content,
    )
  }
}