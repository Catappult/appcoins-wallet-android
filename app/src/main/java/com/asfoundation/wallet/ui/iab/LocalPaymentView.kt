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
}
