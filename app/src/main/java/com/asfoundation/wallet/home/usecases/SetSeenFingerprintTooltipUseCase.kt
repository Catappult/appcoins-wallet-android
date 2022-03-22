package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import javax.inject.Inject

class SetSeenFingerprintTooltipUseCase @Inject constructor(
    private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  operator fun invoke() {
    fingerprintPreferences.setSeenFingerprintTooltip()
  }
}