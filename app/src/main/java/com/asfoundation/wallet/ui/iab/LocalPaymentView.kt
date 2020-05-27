package com.asfoundation.wallet.ui.iab

import android.graphics.Bitmap
import android.os.Bundle
import io.reactivex.Observable

interface LocalPaymentView {
  fun showProcessingLoading()
  fun hideLoading()
  fun showPendingUserPayment(paymentMethodIcon: Bitmap, applicationIcon: Bitmap)
  fun showCompletedPayment()
  fun showError()
  fun dismissError()
  fun getOkErrorClick(): Observable<Any>
  fun getGotItClick(): Observable<Any>
  fun close()
  fun getAnimationDuration(): Long
  fun popView(bundle: Bundle)
  fun lockRotation()
  fun getSupportClicks(): Observable<Any>

  enum class ViewState {
    NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
  }
}
