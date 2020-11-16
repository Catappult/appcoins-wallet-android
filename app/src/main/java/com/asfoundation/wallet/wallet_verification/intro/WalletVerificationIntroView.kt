package com.asfoundation.wallet.wallet_verification.intro

import android.os.Bundle
import io.reactivex.Observable

interface WalletVerificationIntroView {

  fun getCancelClicks(): Observable<Any>

  fun getSubmitClicks(): Observable<Any>

  fun forgetCardClick(): Observable<Any>

  fun cancel()

  fun updateUi(verificationIntroModel: VerificationIntroModel)

  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod, isStored: Boolean,
      forget: Boolean, savedInstance: Bundle?)

  fun showLoading()

  fun showGenericError()

}