package com.asfoundation.wallet.change_currency

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment

class ChangeFiatCurrencyNavigator(private val fragment: Fragment) {

  fun openBottomSheet(fiatCurrency: FiatCurrencyEntity) {
    ChooseCurrencyBottomSheetFragment.newInstance(fiatCurrency)
        .show(fragment.parentFragmentManager, "ChooseCurrencyBottomSheet")
  }
}