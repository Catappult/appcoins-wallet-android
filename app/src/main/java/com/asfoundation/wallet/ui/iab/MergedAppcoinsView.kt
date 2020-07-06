package com.asfoundation.wallet.ui.iab

import androidx.annotation.StringRes
import io.reactivex.Observable

interface MergedAppcoinsView {

  fun showError(@StringRes errorMessage: Int)
  fun getPaymentSelection(): Observable<String>
  fun hideBonus()
  fun showBonus()
  fun buyClick(): Observable<PaymentInfoWrapper>
  fun backClick(): Observable<PaymentInfoWrapper>
  fun backPressed(): Observable<PaymentInfoWrapper>
  fun navigateToAppcPayment()
  fun navigateToCreditsPayment()
  fun navigateToPaymentMethods()
  fun updateBalanceValues(appcFiat: String, creditsFiat: String, currency: String)
  fun showLoading()
  fun hideLoading()
  fun showPaymentMethods()
  fun errorDismisses(): Observable<Any>
  fun getSupportLogoClicks(): Observable<Any>
  fun getSupportIconClicks(): Observable<Any>
}
