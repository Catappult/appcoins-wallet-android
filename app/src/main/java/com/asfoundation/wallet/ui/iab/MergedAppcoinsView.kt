package com.asfoundation.wallet.ui.iab

import androidx.annotation.StringRes
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Observable

interface MergedAppcoinsView {

  fun showError(@StringRes errorMessage: Int)

  fun getPaymentSelection(): Observable<String>

  fun hideBonus()

  fun showBonus()

  fun buyClick(): Observable<PaymentInfoWrapper>

  fun backClick(): Observable<PaymentInfoWrapper>

  fun backPressed(): Observable<PaymentInfoWrapper>

  fun navigateToAppcPayment(transactionBuilder: TransactionBuilder)

  fun navigateToCreditsPayment(transactionBuilder: TransactionBuilder)

  fun updateBalanceValues(appcFiat: String, creditsFiat: String, currency: String)

  fun showLoading()

  fun hideLoading()

  fun errorDismisses(): Observable<Any>

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  fun setPaymentsInformation(hasCredits: Boolean, creditsDisableReason: Int?, hasAppc: Boolean,
                             appcDisabledReason: Int?)

  fun toggleSkeletons(show: Boolean)

  fun showAuthenticationActivity()

  fun onAuthenticationResult(): Observable<Boolean>

  fun showPaymentMethodsView()
}
