package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

class ChooseCurrencyBottomSheetNavigator(private val fragment: ChooseCurrencyBottomSheetFragment) {

  fun navigateBack() {
    fragment.dismiss()
    fragment.requireFragmentManager()
        .popBackStack()
  }
}