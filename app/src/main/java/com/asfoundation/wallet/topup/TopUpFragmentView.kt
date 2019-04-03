package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import io.reactivex.Observable

interface TopUpFragmentView {

  fun getChangeCurrencyClick(): Observable<Any>

  fun getEditTextChanges(): Observable<TopUpData>

  fun getPaymentMethodClick(): Observable<String>

  fun getNextClick(): Observable<TopUpData>

  fun setupUiElements(paymentMethods: List<PaymentMethodData>, localCurrency: LocalCurrency)

  fun setConversionValue(topUpData: TopUpData)

  fun switchCurrencyData()

  fun setNextButtonState(enabled: Boolean)

  fun hideKeyboard()

  fun showLoading()

  fun showPaymentDetailsForm()

  fun showPaymentMethods()

  fun rotateChangeCurrencyButton()

  fun toggleSwitchCurrencyOn()

  fun toggleSwitchCurrencyOff()
}
