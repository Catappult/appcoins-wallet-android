package com.asfoundation.wallet.topup

import android.graphics.Bitmap
import io.reactivex.Observable

interface LocalTopUpPaymentView {
  fun showValues(value: String, currency: String, appcValue: String)

  fun showError()

  fun getSupportIconClicks(): Observable<Any>

  fun getSupportLogoClicks(): Observable<Any>

  fun getGotItClick(): Observable<Any>

  fun getTryAgainClick(): Observable<Any>

  fun retryClick(): Observable<Any>

  fun showProcessingLoading()

  fun showPendingUserPayment(paymentMethodIcon: Bitmap)

  fun navigateToPaymentSelection()

  fun showNetworkError()

  fun showRetryAnimation()

  fun launchPerkBonusService(address: String)
}

enum class ViewState {
  NONE, PENDING_USER_PAYMENT, GENERIC_ERROR, NO_NETWORK, LOADING
}
