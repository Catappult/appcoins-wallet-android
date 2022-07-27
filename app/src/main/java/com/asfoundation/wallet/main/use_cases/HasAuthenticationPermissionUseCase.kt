package com.asfoundation.wallet.main.use_cases

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepository
import javax.inject.Inject

class HasAuthenticationPermissionUseCase @Inject constructor(
  private val fingerprintPreferencesRepository: FingerprintPreferencesRepository
) {

  operator fun invoke(): Boolean = fingerprintPreferencesRepository.hasAuthenticationPermission()

}