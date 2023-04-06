package com.appcoins.wallet.ui.widgets.component

import androidx.compose.ui.graphics.Color
import com.appcoins.wallet.ui.common.theme.WalletColors

enum class ButtonTypes(
  val backgroundColor: Color = Color.Transparent,
  val labelColor: Color,
  val outlineColor: Color? = null
) {
  FILLED_PINK_BUTTON(
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white
  ),
  FILLED_BLUE_BUTTON(
    backgroundColor = WalletColors.styleguide_blue,
    labelColor = WalletColors.styleguide_pink
  ),
  FILLED_GOLD_BUTTON(
    backgroundColor = WalletColors.styleguide_vip_yellow,
    labelColor = WalletColors.styleguide_black
  ),
  FILLED_GREY_BUTTON(
    backgroundColor = WalletColors.styleguide_medium_grey,
    labelColor = WalletColors.styleguide_white
  ),
  OUTLINED_PINK_BUTTON(
    outlineColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white
  ),
  OUTLINED_WHITE_BUTTON(
    outlineColor = WalletColors.styleguide_white,
    labelColor = WalletColors.styleguide_white
  )
}