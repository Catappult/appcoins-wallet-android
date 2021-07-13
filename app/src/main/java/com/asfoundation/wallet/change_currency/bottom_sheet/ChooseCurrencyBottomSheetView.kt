package com.asfoundation.wallet.change_currency.bottom_sheet

import io.reactivex.Observable

interface ChooseCurrencyBottomSheetView {

  fun setCurrencyFlag(currencyFlag: String)

  fun setCurrencyShort(currencyShort: String)

  fun setCurrencyLabel(currencyLabel: String)

  fun getConfirmationClick(): Observable<Any>

  fun showLoading()
}