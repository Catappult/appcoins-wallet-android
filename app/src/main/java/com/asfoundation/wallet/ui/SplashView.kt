package com.asfoundation.wallet.ui

import androidx.biometric.BiometricPrompt
import io.reactivex.Observable

interface SplashView {
  fun navigateToAutoUpdate()
  fun firstScreenNavigation()
  fun createBiometricPrompt(): BiometricPrompt
  fun getAuthenticationResult(): Observable<FingerprintAuthResult>
  fun showBottomSheetDialogFragment(message: CharSequence?)
  fun showFail()
  fun showPrompt(biometricPrompt: BiometricPrompt, promptInfo: BiometricPrompt.PromptInfo)
  fun getRetryButtonClick(): Observable<Any>
  fun onRetryButtonClick()
  fun checkBiometricSupport(): Boolean

}
