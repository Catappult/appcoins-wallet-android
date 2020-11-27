package com.asfoundation.wallet.verification.code

import android.os.Bundle
import io.reactivex.Observable

interface WalletVerificationCodeView {

  fun updateUi(verificationCodeData: VerificationCodeData, savedInstance: Bundle?)

  fun showLoading()

  fun hideLoading()

  fun hideKeyboard()

  fun lockRotation()

  fun unlockRotation()

  fun showSuccess()

  fun showGenericError()

  fun showNetworkError()

  fun showSpecificError(stringRes: Int)

  fun getMaybeLaterClicks(): Observable<Any>

  fun getChangeCardClicks(): Observable<Any>

  fun getConfirmClicks(): Observable<String>

  fun getTryAgainClicks(): Observable<Any>

  fun getSupportClicks(): Observable<Any>

}