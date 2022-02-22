package com.asfoundation.wallet.verification.ui.credit_card.intro

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel.ErrorType
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import io.reactivex.Observable

interface VerificationIntroView {

  fun getCancelClicks(): Observable<Any>

  fun retryClick(): Observable<Any>

  fun getSubmitClicks(): Observable<Any>

  fun getTryAgainClicks(): Observable<Any>

  fun getSupportClicks(): Observable<Any>

  fun forgetCardClick(): Observable<Any>

  fun cancel()

  fun updateUi(verificationIntroModel: VerificationIntroModel)

  fun finishCardConfiguration(
    paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod, isStored: Boolean,
    forget: Boolean, savedInstance: Bundle?
  )

  fun retrievePaymentData(): Observable<AdyenCardWrapper>

  fun showLoading()

  fun hideLoading()

  fun hideKeyboard()

  fun lockRotation()

  fun unlockRotation()

  fun showError(errorType: ErrorType? = ErrorType.OTHER)

  fun showNetworkError()

  fun showSpecificError(stringRes: Int)

  fun showCvvError()

  fun showGenericError()
}