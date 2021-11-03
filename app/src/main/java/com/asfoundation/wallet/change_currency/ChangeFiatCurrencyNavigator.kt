package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment

class ChangeFiatCurrencyNavigator(private val fragment: ChangeFiatCurrencyFragment) {

  fun openBottomSheet(fiatCurrency: FiatCurrencyEntity) {
    ChooseCurrencyBottomSheetFragment.newInstance(fiatCurrency)
        .show(fragment.parentFragmentManager, "ChooseCurrencyBottomSheet")
  }
}