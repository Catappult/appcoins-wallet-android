package com.asfoundation.wallet.util

import com.adyen.threeds2.customization.ButtonCustomization
import com.adyen.threeds2.customization.ToolbarCustomization
import com.adyen.threeds2.customization.UiCustomization

class Adyen3DSUiUtils {

  companion object {
    const val MAIN_COLOR = "#111B3B"
    const val STATUS_BAR_COLOR = "#154868"
    const val WHITE = "#FFFFFF"

    fun createUiCustomization(): UiCustomization {
      val uiCustomization = UiCustomization()
      val toolbarCustomization = ToolbarCustomization()
      toolbarCustomization.backgroundColor = MAIN_COLOR
      toolbarCustomization.textColor = WHITE
      val positiveButtonCustomization = ButtonCustomization()
      positiveButtonCustomization.backgroundColor = MAIN_COLOR
      val negativeButtonCustomization = ButtonCustomization()
      negativeButtonCustomization.textColor = MAIN_COLOR
      uiCustomization.toolbarCustomization = toolbarCustomization
      uiCustomization.setButtonCustomization(positiveButtonCustomization,
          UiCustomization.ButtonType.CONTINUE)
      uiCustomization.setButtonCustomization(positiveButtonCustomization,
          UiCustomization.ButtonType.VERIFY)
      uiCustomization.setButtonCustomization(positiveButtonCustomization,
          UiCustomization.ButtonType.NEXT)
      uiCustomization.setButtonCustomization(negativeButtonCustomization,
          UiCustomization.ButtonType.CANCEL)
      uiCustomization.setButtonCustomization(negativeButtonCustomization,
          UiCustomization.ButtonType.RESEND)
      uiCustomization.setStatusBarColor(STATUS_BAR_COLOR)
      return uiCustomization
    }
  }

}