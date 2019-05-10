package com.asfoundation.wallet.ui.onboarding

import io.reactivex.Observable

interface OnboardingView {

  fun setupUi()

  fun showLoading()

  fun finishOnboarding()

  fun getSkipButtonClick(): Observable<Any>

}