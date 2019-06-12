package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable

interface LocalPaymentView {
  fun showProcessingLoading()
  fun hideLoading()
  fun showPendingUserPayment()
  fun showCompletedPayment()
  fun showError()
  fun dismissError()
  fun getOkErrorClick(): Observable<Any>
  fun getOkBuyClick(): Observable<Any>
  fun close()

  enum class ViewState {
    NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
  }
}
