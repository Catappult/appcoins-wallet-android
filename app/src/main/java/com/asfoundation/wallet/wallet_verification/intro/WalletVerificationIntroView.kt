package com.asfoundation.wallet.wallet_verification.intro

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

interface WalletVerificationIntroView {

  fun getCancelClicks(): Observable<Any>

  fun getSubmitClicks(): Observable<Any>

  fun forgetCardClick(): Observable<Any>

  fun cancel()

  fun updateUi(verificationIntroModel: VerificationIntroModel)

  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod, isStored: Boolean,
      forget: Boolean, savedInstance: Bundle?)

  fun retrievePaymentData(): ReplaySubject<AdyenCardWrapper>

  fun showLoading()

  fun showGenericError()

  fun hideKeyboard()

  fun lockRotation()

  fun unlockRotation()

}