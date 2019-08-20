package com.asfoundation.wallet.wallet_validation.generic

import io.reactivex.Observable

interface PhoneValidationView {

  fun setupUI()

  fun getCountryCode(): Observable<String>

  fun getPhoneNumber(): Observable<String>

  fun setButtonState(state: Boolean)

  fun getNextClicks(): Observable<Pair<String, String>>

  fun getCancelClicks(): Observable<Any>

  fun setError(message: Int)

  fun clearError()

}