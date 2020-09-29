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

  fun updateBalanceValues(appcFiat: String, creditsFiat: String, currency: String)

  fun showLoading()

  fun hideLoading()

  fun errorDismisses(): Observable<Any>

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  fun setPaymentsInformation(hasCredits: Boolean, creditsDisableReason: Int?, hasAppc: Boolean,
                             appcDisabledReason: Int?)

  fun toggleSkeletons(show: Boolean)
}
