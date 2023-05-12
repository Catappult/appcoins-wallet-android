package com.asfoundation.wallet.verification.ui.credit_card.code

import android.os.Bundle
import io.reactivex.Observable

interface VerificationCodeView {

  fun setupUi(currency: String, symbol: String, amount: String, digits: Int, format: String,
              period: String, date: Long, isWalletVerified: Boolean, savedInstance: Bundle?)

  fun showLoading()

  fun hideLoading()

  fun hideKeyboard()

  fun lockRotation()

  fun unlockRotation()

  fun showSuccess()

  fun showNetworkError()

  fun getMaybeLaterClicks(): Observable<Any>

  fun getChangeCardClicks(): Observable<Any>

  fun getConfirmClicks(): Observable<String>

  fun retryClick(): Observable<Any>

  fun showWrongCodeError()

  fun showVerificationCode()
}