package com.asfoundation.wallet.topup

import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import io.reactivex.Observable

interface TopUpFragmentView {

  fun getChangeCurrencyClick(): Observable<Any>

  fun getEditTextChanges(): InitialValueObservable<TextViewAfterTextChangeEvent>

  fun getPaymentMethodClick(): Observable<String>

  fun getEditTextFocusChanges(): InitialValueObservable<Boolean>

  fun getNextClick(): Observable<Any>

  fun setupUiElements(data: TopUpData)

  fun updateCurrencyData(data: CurrencyData)

  fun setNextButtonState(enabled: Boolean)

  fun hideKeyboard()

  fun showLoading()

  fun showPaymentDetailsForm()

  fun showPaymentMethods()

  fun rotateChangeCurrencyButton()

}
