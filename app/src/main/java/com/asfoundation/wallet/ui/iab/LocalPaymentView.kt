package com.asfoundation.wallet.ui.iab

import android.os.Bundle
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
  fun getAnimationDuration(): Long
  fun popView(bundle: Bundle)
  fun lockRotation()

  enum class ViewState {
    NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
  }
}
