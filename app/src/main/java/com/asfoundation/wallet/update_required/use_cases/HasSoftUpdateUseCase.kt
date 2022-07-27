package com.asfoundation.wallet.update_required.use_cases

import javax.inject.Inject
import javax.inject.Named

class HasSoftUpdateUseCase @Inject constructor(
  @Named("local_version_code")
  private val walletVersionCode: Int,
  @Named("device-sdk")
  private val deviceSdk: Int,
) {

  operator fun invoke(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return walletVersionCode < updateVersionCode && deviceSdk >= updatedMinSdk
  }
}