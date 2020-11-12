package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import io.reactivex.Observable

interface OnboardingView {

  fun showLoading()

  fun finishOnboarding(showAnimation: Boolean)

  fun getNextButtonClick(): Observable<Any>

  fun getLinkClick(): Observable<String>

  fun getSkipClicks(): Observable<Any>

  fun navigateToBrowser(uri: Uri)

  fun showViewPagerLastPage()

  fun setPaymentMethodsIcons(paymentMethodsIcons: List<String>)

  fun showWarningText()
}