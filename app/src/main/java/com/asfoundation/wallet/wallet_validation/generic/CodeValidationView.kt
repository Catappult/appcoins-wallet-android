package com.asfoundation.wallet.wallet_validation.generic

import com.asfoundation.wallet.wallet_validation.ValidationInfo
import io.reactivex.Observable

interface CodeValidationView {

  fun setupUI()

  fun clearUI()

  fun getBackClicks(): Observable<Any>

  fun getSubmitClicks(): Observable<ValidationInfo>

  fun getResentCodeClicks(): Observable<Any>

  fun getFirstChar(): Observable<String>

  fun getSecondChar(): Observable<String>

  fun getThirdChar(): Observable<String>

  fun getFourthChar(): Observable<String>

  fun getFifthChar(): Observable<String>

  fun getSixthChar(): Observable<String>

  fun moveToNextView(current: Int)

  fun setButtonState(state: Boolean)

  fun hideKeyboard()

  fun showLoading()

  fun showReferralEligible()

  fun showReferralIneligible()

  fun getOkClicks(): Observable<Any>

}