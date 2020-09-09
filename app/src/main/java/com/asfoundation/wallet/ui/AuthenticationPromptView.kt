package com.asfoundation.wallet.ui

import androidx.biometric.BiometricPrompt
import io.reactivex.Observable

interface AuthenticationPromptView {
  fun createBiometricPrompt(): BiometricPrompt
  fun getAuthenticationResult(): Observable<FingerprintAuthResult>
  fun showAuthenticationBottomSheet(timer: Long)
  fun showFail()
  fun showPrompt(biometricPrompt: BiometricPrompt, promptInfo: BiometricPrompt.PromptInfo)
  fun getRetryButtonClick(): Observable<Any>
  fun onRetryButtonClick()
  fun checkBiometricSupport(): Boolean
  fun closeSuccess()
  fun closeCancel()
}
