package com.asfoundation.wallet.ui.iab

import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.StringRes
import io.reactivex.Observable

interface LocalPaymentView {

  fun showProcessingLoading()

  fun hideLoading()

  fun showPendingUserPayment(paymentMethodIcon: Bitmap, applicationIcon: Bitmap)

  fun showCompletedPayment()

  fun showError(message: Int? = null)

  fun dismissError()

  fun getErrorDismissClick(): Observable<Any>

  fun getGotItClick(): Observable<Any>

  fun close()

  fun getAnimationDuration(): Long

  fun popView(bundle: Bundle)

  fun lockRotation()

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  fun showWalletValidation(@StringRes error: Int)

  fun launchPerkBonusService(address: String)

  enum class ViewState {
    NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
  }
}
