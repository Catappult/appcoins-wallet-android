package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import io.reactivex.Observable

interface OnboardingView {

  fun setupUi()

  fun showLoading()

  fun finishOnboarding()

  fun getSkipButtonClick(): Observable<Any>

  fun getLinkClick(): Observable<String>?

  fun navigateToBrowser(uri: Uri)

}