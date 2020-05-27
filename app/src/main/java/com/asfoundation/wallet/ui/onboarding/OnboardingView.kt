package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable

interface OnboardingView {

  fun updateUI(maxAmount: String, isActive: Boolean)

  fun showLoading()

  fun finishOnboarding(walletValidationStatus: WalletValidationStatus, showAnimation: Boolean)

  fun getNextButtonClick(): Observable<Any>

  fun getRedeemButtonClick(): Observable<Any>

  fun getLinkClick(): Observable<String>

  fun getSkipClicks(): Observable<Any>

  fun navigateToBrowser(uri: Uri)

  fun showNoInternetView()

  fun showViewPagerLastPage()

  fun setPaymentMethodsIcons(paymentMethodsIcons: List<String>)

  fun getRetryButtonClicks(): Observable<Any>

  fun getLaterButtonClicks(): Observable<Any>

  fun showWarningText()

}