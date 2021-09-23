package com.asfoundation.wallet.change_currency.bottom_sheet

class ChooseCurrencyBottomSheetNavigator(private val fragment: ChooseCurrencyBottomSheetFragment) {

  fun navigateBack() {
    fragment.activity?.finish()
  }
}