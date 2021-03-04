package com.asfoundation.wallet.verification.error

import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import io.reactivex.Observable

interface VerificationErrorView {
  fun initializeView(errorType: VerificationCodeResult.ErrorType, amount: String, symbol: String)

  fun getMaybeLaterClicks(): Observable<Any>

  fun getTryAgainClicks(): Observable<Any>

  fun getTryAgainAttemptsClicks(): Observable<Any>
}