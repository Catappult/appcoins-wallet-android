package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable

interface OnboardingView {

  fun setupUi()

  fun showLoading()

  fun finishOnboarding(walletValidationStatus: WalletValidationStatus)

  fun getNextButtonClick(): Observable<Any>

  fun getLinkClick(): Observable<String>?

  fun navigateToBrowser(uri: Uri)

}