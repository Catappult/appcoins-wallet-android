package com.asfoundation.wallet.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.asf.wallet.R
import com.asfoundation.wallet.repository.PreferencesRepositoryType

class FingerPrintInteractor(private val context: Context,
                            private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun compatibleDevice(): Int {
    return BiometricManager.from(context)
        .canAuthenticate()
  }

  fun definePromptInformation(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.fingerprint_authentication_required_title))
        .setSubtitle(context.getString(R.string.fingerprint_authentication_required_body))
        .setDeviceCredentialAllowed(true)
        .build()
  }

  fun setAuthenticationPermission(value: Boolean) =
      preferencesRepositoryType.setAuthenticationPermission(value)

  fun getAuthenticationErrorTime() = preferencesRepositoryType.getAuthenticationErrorTime()

  fun setAuthenticationErrorTime(currentTime: Long) =
      preferencesRepositoryType.setAuthenticationErrorTime(currentTime)
}
