package com.asfoundation.wallet.change_currency

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment
import javax.inject.Inject

class ChangeFiatCurrencyNavigator @Inject constructor(private val fragment: Fragment) {

  fun openBottomSheet(fiatCurrency: FiatCurrencyEntity) {
    ChooseCurrencyBottomSheetFragment.newInstance(fiatCurrency)
        .show(fragment.parentFragmentManager, "ChooseCurrencyBottomSheet")
  }
}