package com.asfoundation.wallet.topup

import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import io.reactivex.Observable

interface TopUpFragmentView {

  fun setupUiElements(data: UiData)

  fun getChangeCurrencyClick(): Observable<Any>

  fun getEditTextChanges(): InitialValueObservable<TextViewAfterTextChangeEvent>

  fun changeCurrency(data: CurrencyData)

}
