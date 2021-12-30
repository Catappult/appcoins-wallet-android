package com.asfoundation.wallet.change_currency.bottom_sheet

import androidx.fragment.app.Fragment

class ChooseCurrencyBottomSheetNavigator(private val fragment: Fragment) {

  fun navigateBack() {
    fragment.activity?.finish()
  }
}