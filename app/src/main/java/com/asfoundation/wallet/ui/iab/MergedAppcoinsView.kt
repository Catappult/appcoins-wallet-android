package com.asfoundation.wallet.ui.iab

import androidx.annotation.StringRes
import io.reactivex.Observable

interface MergedAppcoinsView {

  fun showError(@StringRes errorMessage: Int)
  fun getPaymentSelection(): Observable<String>
  fun hideBonus()
  fun showBonus()
  fun buyClick(): Observable<String>
  fun backClick(): Observable<Any>
  fun backPressed(): Observable<Any>
  fun navigateToAppcPayment()
  fun navigateToCreditsPayment()
  fun navigateToPaymentMethods()
  fun updateBalanceValues(appcFiat: FiatValue, creditsFiat: FiatValue)
  fun showWalletBlocked()
  fun showLoading()
  fun hideLoading()
  fun showPaymentMethods()
}
