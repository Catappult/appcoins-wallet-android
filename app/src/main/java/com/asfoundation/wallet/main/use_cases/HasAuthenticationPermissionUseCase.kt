package com.asfoundation.wallet.main.use_cases

import fingerprint.FingerprintPreferencesDataSource
import javax.inject.Inject

class HasAuthenticationPermissionUseCase @Inject constructor(
  private val fingerprintPreferencesDataSource: FingerprintPreferencesDataSource
) {

  operator fun invoke(): Boolean = fingerprintPreferencesDataSource.hasAuthenticationPermission()

}