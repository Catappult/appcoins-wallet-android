package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable

interface OnboardingView {

  fun setupUi()

  fun showLoading()

  fun finishOnboarding(walletValidationStatus: WalletValidationStatus?, showAnimation: Boolean)

  fun navigate(walletValidationStatus: WalletValidationStatus?)

  fun getNextButtonClick(): Observable<Any>

  fun getRedeemButtonClick(): Observable<Any>

  fun getLinkClick(): Observable<String>?

  fun navigateToBrowser(uri: Uri)

  fun showNoInternetView()

  fun getRetryButtonClicks(): Observable<Any>

  fun getLaterButtonClicks(): Observable<Any>

}