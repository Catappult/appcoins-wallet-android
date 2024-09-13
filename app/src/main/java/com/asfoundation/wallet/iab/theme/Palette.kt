package com.asfoundation.wallet.iab.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object LightPalette {
  val CoralBlush = Color(0xFFFF6381)
  val MidnightVelvet = Color(0xFF1A1A24)
  val MidnightEclipse = Color(0xFF242333)
  val TropicalOasis = Color(0xFF3CBD8F)
  val SunsetGoldVIP = Color(0xFFF9B622)
  val StormyLaggon = Color(0xFF8E93A1)
  val SilverMist = Color(0xFFC9C9C9)
  val WinterWhisper = Color(0xFFF5F5FA)
  val Error = Color(0xFFEA001B)
  val White = Color.White
}

object DarkPalette {
  val CoralBlush = Color(0xFFFF6381)
  val MidnightVelvet = Color(0xFF1A1A24)
  val MidnightEclipse = Color(0xFF242333)
  val TropicalOasis = Color(0xFF3CBD8F)
  val SunsetGoldVIP = Color(0xFFF9B622)
  val StormyLaggon = Color(0xFF8E93A1)
  val SilverMist = Color(0xFFC9C9C9)
  val WinterWhisper = Color(0xFFF5F5FA)
  val Error = Color(0xFFEA001B)
  val Black = Color.Black
}

val lightColorPalette = IAPColors(
  backArrow = LightPalette.MidnightEclipse,
  smallText = LightPalette.StormyLaggon,
  disabledBColor = LightPalette.StormyLaggon,
  transparentButtonText = LightPalette.MidnightEclipse,
  arrowColor = LightPalette.StormyLaggon,
  lineColor = LightPalette.StormyLaggon,
  materialColors = lightColorScheme(
    primary = LightPalette.WinterWhisper,
    onPrimary = LightPalette.MidnightEclipse,
    primaryContainer = LightPalette.White,
    secondary = LightPalette.CoralBlush,
    onSecondary = LightPalette.White,
    onError = LightPalette.Error
  )
)

val darkColorPalette = IAPColors(
  backArrow = DarkPalette.MidnightEclipse,
  smallText = DarkPalette.StormyLaggon,
  disabledBColor = LightPalette.StormyLaggon,
  transparentButtonText = LightPalette.MidnightEclipse,
  arrowColor = LightPalette.StormyLaggon,
  lineColor = LightPalette.StormyLaggon,
  materialColors = darkColorScheme(
    primary = DarkPalette.WinterWhisper,
    onPrimary = DarkPalette.MidnightEclipse,
    primaryContainer = LightPalette.White,
    secondary = DarkPalette.CoralBlush,
    onSecondary = LightPalette.White,
    onError = LightPalette.Error
  )
)

data class IAPColors(
  val backArrow: Color,
  val smallText: Color,
  val disabledBColor: Color,
  val transparentButtonText: Color,
  val arrowColor: Color,
  val lineColor: Color,
  val materialColors: ColorScheme
) {
  val primary: Color
    get() = materialColors.primary // Used for main background
  val onPrimary: Color
    get() = materialColors.onPrimary //
  val primaryContainer: Color
    get() = materialColors.primaryContainer // Used for backgrounds of floating views
  val secondary: Color
    get() = materialColors.secondary // Used for buttons background
  val onSecondary: Color
    get() = materialColors.onSecondary // Used for buttons text
  val onError: Color
    get() = materialColors.onError // Used for error text
}
