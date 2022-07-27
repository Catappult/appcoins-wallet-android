package com.asfoundation.wallet.update_required.use_cases

import javax.inject.Inject
import javax.inject.Named

class HasRequiredHardUpdateUseCase @Inject constructor(
  @Named("local_version_code")
  private val walletVersionCode: Int,
  private val hasSoftUpdateUseCase: HasSoftUpdateUseCase
) {

  operator fun invoke(
    blackList: List<Int>, updateVersionCode: Int,
    updateMinSdk: Int
  ): Boolean {
    return blackList.contains(walletVersionCode) && hasSoftUpdateUseCase(
      updateVersionCode,
      updateMinSdk
    )
  }
}