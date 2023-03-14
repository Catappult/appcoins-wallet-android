package com.asfoundation.wallet.home.usecases

import fingerprint.FingerprintPreferencesDataSource
import javax.inject.Inject

class SetSeenFingerprintTooltipUseCase @Inject constructor(
  private val fingerprintPreferences: FingerprintPreferencesDataSource
) {

  operator fun invoke() {
    fingerprintPreferences.setSeenFingerprintTooltip()
  }
}