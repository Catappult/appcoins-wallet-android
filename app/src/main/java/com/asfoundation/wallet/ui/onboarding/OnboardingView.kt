package com.asfoundation.wallet.ui.onboarding

import io.reactivex.Observable

interface OnboardingView {
  fun getOkClick(): Observable<Any>
  fun getSkipClick(): Observable<Any>
  fun getCheckboxClick(): Observable<Any>
}