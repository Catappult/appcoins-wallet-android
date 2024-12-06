package com.asfoundation.wallet.billing.amazonPay.usecases


import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import javax.inject.Inject

class SaveAmazonPayChargePermissionLocalStorageUseCase @Inject constructor(
  private val amazonPayRepository: AmazonPayRepository,
) {

  operator fun invoke(chargePermissionId: String?) =
    amazonPayRepository.saveChargePermissionId(chargePermissionId)
}
