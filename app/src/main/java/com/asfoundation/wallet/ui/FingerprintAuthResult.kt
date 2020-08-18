package com.asfoundation.wallet.ui

import androidx.biometric.BiometricPrompt


data class FingerprintAuthResult(val errorCode: Int?,
                                 val errorString: String?,
                                 val result: BiometricPrompt.AuthenticationResult?,
                                 val type: FingerprintResult)

enum class FingerprintResult {
  SUCCESS, ERROR, FAIL
}
