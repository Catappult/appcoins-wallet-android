package com.asfoundation.wallet.ui

import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_FINGERPRINT
import android.os.Build
import androidx.biometric.BiometricManager
import fingerprint.FingerprintPreferencesDataSource
import javax.inject.Inject

class FingerprintInteractor @Inject constructor(
  private val biometricManager: BiometricManager,
  private val packageManager: PackageManager,
  private val fingerprintPreferences: FingerprintPreferencesDataSource
) {

  fun getDeviceCompatibility(): Int {
    val biometricCompatibility = biometricManager.canAuthenticate()
    //User may have biometrics but no fingerprint (e.g face recognition)
    if (hasBiometrics(biometricCompatibility) && !hasFingerPrint()) {
      return BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }
    return biometricCompatibility
  }

  fun setAuthenticationPermission(value: Boolean) =
      fingerprintPreferences.setAuthenticationPermission(value)

  fun getAuthenticationErrorTime() = fingerprintPreferences.getAuthenticationErrorTime()

  fun setAuthenticationErrorTime(currentTime: Long) =
      fingerprintPreferences.setAuthenticationErrorTime(currentTime)

  private fun hasBiometrics(biometricCompatibility: Int): Boolean {
    return (biometricCompatibility == BiometricManager.BIOMETRIC_SUCCESS || biometricCompatibility == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
  }

  private fun hasFingerPrint(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      packageManager.hasSystemFeature(FEATURE_FINGERPRINT)
    } else {
      false
    }
  }
}
