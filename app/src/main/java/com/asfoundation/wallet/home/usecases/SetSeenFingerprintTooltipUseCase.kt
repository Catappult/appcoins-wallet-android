package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract

class SetSeenFingerprintTooltipUseCase(
    private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  operator fun invoke() {
    fingerprintPreferences.setSeenFingerprintTooltip()
  }
}