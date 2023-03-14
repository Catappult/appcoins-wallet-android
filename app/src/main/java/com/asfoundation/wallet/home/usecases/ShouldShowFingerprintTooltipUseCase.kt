package com.asfoundation.wallet.home.usecases

import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import fingerprint.FingerprintPreferencesDataSource
import repository.PreferencesRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class ShouldShowFingerprintTooltipUseCase @Inject constructor(
  private val preferencesRepositoryType: PreferencesRepositoryType,
  private val packageManager: PackageManager,
  private val fingerprintPreferences: FingerprintPreferencesDataSource,
  private val biometricManager: BiometricManager
) {

  private companion object {
    private const val UPDATE_FINGERPRINT_NUMBER_OF_TIMES = 3
  }

  operator fun invoke(packageName: String): Single<Boolean> {
    var shouldShow = false
    if (!preferencesRepositoryType.hasBeenInSettings() && !fingerprintPreferences.hasSeenFingerprintTooltip()
      && hasFingerprint() && !fingerprintPreferences.hasAuthenticationPermission() &&
      preferencesRepositoryType.hasSeenPromotionTooltip()
    ) {
      if (!isFirstInstall(packageName)) {
        shouldShow = true
      } else if (getNumberOfTimesOnHome() >= UPDATE_FINGERPRINT_NUMBER_OF_TIMES) {
        shouldShow = true
      }
    }
    return Single.just(shouldShow)
  }

  private fun isFirstInstall(packageName: String): Boolean {
    return try {
      val firstInstallTime: Long = packageManager.getPackageInfo(packageName, 0).firstInstallTime
      val lastUpdateTime: Long = packageManager.getPackageInfo(packageName, 0).lastUpdateTime
      firstInstallTime == lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      true
    }
  }

  private fun hasFingerprint(): Boolean {
    return getDeviceCompatibility() == BiometricManager.BIOMETRIC_SUCCESS
  }

  //TODO duplicated code from FingerprintInteractor
  fun getDeviceCompatibility(): Int {
    val biometricCompatibility = biometricManager.canAuthenticate()
    //User may have biometrics but no fingerprint (e.g face recognition)
    if (hasBiometrics(biometricCompatibility) && !hasFingerPrint()) {
      return BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }
    return biometricCompatibility
  }

  private fun hasBiometrics(biometricCompatibility: Int): Boolean {
    return (biometricCompatibility == BiometricManager.BIOMETRIC_SUCCESS || biometricCompatibility == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
  }

  private fun hasFingerPrint(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    } else {
      false
    }
  }

  private fun getNumberOfTimesOnHome(): Int = preferencesRepositoryType.getNumberOfTimesOnHome()
}