package com.appcoins.wallet.ui.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class TypographySize(
  val sp26: TextStyle,
  val sp24: TextStyle,
  val sp22: TextStyle,
  val sp16: TextStyle,
  val sp14: TextStyle,
  val sp12: TextStyle,
  val XXS: TextStyle
)

data class TypographyType(
  val regular: TypographySize,
  val medium: TypographySize,
  val bold: TypographySize,
)

val WalletTypography = TypographyType(
  regular = TypographySize(
    sp26 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 26.sp,
      lineHeight = 34.sp,
    ),
    sp24 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
    ),
    sp22 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 22.sp,
      lineHeight = 30.sp,
    ),
    sp16 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    sp14 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    sp12 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    XXS = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 10.sp,
      lineHeight = 14.sp,
    ),
  ),
  medium = TypographySize(
    sp26 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 26.sp,
      lineHeight = 34.sp,
    ),
    sp24 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 24.sp,
      lineHeight = 32.sp,
    ),
    sp22 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 22.sp,
      lineHeight = 30.sp,
    ),
    sp16 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    sp14 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    sp12 = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    XXS = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 10.sp,
      lineHeight = 14.sp,
    ),
  ),
  bold = TypographySize(
    sp26 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 26.sp,
      lineHeight = 34.sp,
    ),
    sp24 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      lineHeight = 32.sp,
    ),
    sp22 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      lineHeight = 30.sp,
    ),
    sp16 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    sp14 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    sp12 = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    XXS = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      lineHeight = 14.sp,
    ),
  )
)

val Tytutut = Typography(
  displayLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
  ),
  displayMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  displaySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
  ),
  headlineLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
  ),
  headlineSmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  ),
  titleLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.1.sp,
  ),
  titleSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),
  labelLarge = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
  ),
)
