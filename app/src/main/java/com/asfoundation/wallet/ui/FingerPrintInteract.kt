package com.asfoundation.wallet.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

class FingerPrintInteract(private val context: Context) {

  fun compatibleDevice(): Int {
    return BiometricManager.from(context)
        .canAuthenticate()
  }

  fun definePromptInformation(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authentication Required!")
        .setSubtitle("Please enter your credential.")
        .setDeviceCredentialAllowed(true)
        .build()
  }

}
