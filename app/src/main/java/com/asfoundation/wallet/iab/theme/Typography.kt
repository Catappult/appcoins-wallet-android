package com.asfoundation.wallet.iab.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.R

private val robotoFontFamily = FontFamily(
  Font(R.font.roboto_bolt, FontWeight.Bold),
  Font(R.font.roboto_medium, FontWeight.Normal),
  Font(R.font.roboto_regular, FontWeight.Light),
)

object WalletTypography {
  val XXS = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(500),
    fontSize = 10.sp,
    lineHeight = 12.sp,
  )
  val XS = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(500),
    fontSize = 12.sp,
    lineHeight = 14.sp
  )
  val S = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(500),
    fontSize = 14.sp,
    lineHeight = 16.sp
  )
  val M = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(500),
    fontSize = 16.sp,
    lineHeight = 18.sp
  )
  val L = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(500),
    fontSize = 22.sp,
    lineHeight = 26.sp
  )
  val XL = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(700),
    fontSize = 24.sp,
    lineHeight = 28.sp
  )
  val XXL = TextStyle(
    fontFamily = robotoFontFamily,
    fontWeight = FontWeight(700),
    fontSize = 26.sp,
    lineHeight = 30.sp
  )
}

val lightTypography = IAPTypography(
  typography = Typography(
    headlineLarge = WalletTypography.XXL,
    headlineMedium = WalletTypography.XL,
    titleLarge = WalletTypography.L,
    titleMedium = WalletTypography.M,
    bodyLarge = WalletTypography.S,
    bodyMedium = WalletTypography.XS,
    bodySmall = WalletTypography.XXS,
  )
)

val darkTypography = IAPTypography(
  typography = Typography(
    headlineLarge = WalletTypography.XXL,
    headlineMedium = WalletTypography.XL,
    titleLarge = WalletTypography.L,
    titleMedium = WalletTypography.M,
    bodyLarge = WalletTypography.S,
    bodyMedium = WalletTypography.XS,
    bodySmall = WalletTypography.XXS,
  )
)

data class IAPTypography(
  val typography: Typography
) {
  val headlineLarge: TextStyle
    get() = typography.headlineLarge
  val headlineMedium: TextStyle
    get() = typography.headlineMedium
  val titleLarge: TextStyle
    get() = typography.titleLarge
  val titleMedium: TextStyle
    get() = typography.titleMedium
  val bodyLarge: TextStyle
    get() = typography.bodyLarge
  val bodyMedium: TextStyle
    get() = typography.bodyMedium
  val bodySmall: TextStyle
    get() = typography.bodySmall
}
