package com.asfoundation.wallet.wallet_validation.generic

import io.reactivex.Observable

interface PhoneValidationView {

  fun setupUI()

  fun getCountryCode(): Observable<String>

  fun getPhoneNumber(): Observable<String>

  fun setButtonState(state: Boolean)

  fun getNextClicks(): Observable<PhoneValidationFragment.Companion.PhoneValidationClickData>

  fun getCancelClicks(): Observable<PhoneValidationFragment.Companion.PhoneValidationClickData>

  fun setError(message: Int)

  fun clearError()

  fun showNoInternetView()

  fun hideNoInternetView()

  fun getRetryButtonClicks(): Observable<PhoneValidationFragment.Companion.PhoneValidationClickData>

  fun getLaterButtonClicks(): Observable<PhoneValidationFragment.Companion.PhoneValidationClickData>

  fun hideKeyboard()

}