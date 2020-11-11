package com.asfoundation.wallet.ui.iab.local_payments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.StringRes
import io.reactivex.Observable

interface LocalPaymentView {

  fun showProcessingLoading()

  fun hideLoading()

  fun showPendingUserPayment(paymentMethodLabel: String?, paymentMethodIcon: Bitmap,
                             applicationIcon: Bitmap)

  fun showCompletedPayment()

  fun showError(message: Int? = null)

  fun dismissError()

  fun getErrorDismissClick(): Observable<Any>

  fun getGotItClick(): Observable<Any>

  fun close()

  fun getAnimationDuration(): Long

  fun popView(bundle: Bundle, paymentId: String)

  fun lockRotation()

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  fun showWalletValidation(@StringRes error: Int)

  fun setupUi(bonus: String?)

  enum class ViewState {
    NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
  }
}
