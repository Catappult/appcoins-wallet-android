package com.asfoundation.wallet.change_currency.bottom_sheet

import androidx.fragment.app.Fragment
import javax.inject.Inject

class ChooseCurrencyBottomSheetNavigator @Inject constructor(private val fragment: Fragment) {

  fun navigateBack() {
    fragment.activity?.finish()
  }
}