package com.asfoundation.wallet.change_currency.bottom_sheet

interface ChooseCurrencyBottomSheetView {

  fun setCurrencyFlag(currencyFlag: String)

  fun setCurrencyShort(currencyShort: String)

  fun setCurrencyLabel(currencyLabel: String)

  fun showLoading(shouldShow: Boolean)
}