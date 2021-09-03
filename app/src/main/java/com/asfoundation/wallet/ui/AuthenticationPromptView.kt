package com.asfoundation.wallet.ui

import androidx.biometric.BiometricPrompt
import io.reactivex.Observable

interface AuthenticationPromptView {

  fun createBiometricPrompt(): BiometricPrompt

  fun getAuthenticationResult(): Observable<FingerprintAuthResult>

  fun showAuthenticationBottomSheet(timer: Long)

  fun showPrompt(biometricPrompt: BiometricPrompt, deviceCredentialsAllowed: Boolean)

  fun getRetryButtonClick(): Observable<Any>

  fun onRetryButtonClick()

  fun closeSuccess()

  fun closeCancel()
}
