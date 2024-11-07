package com.asfoundation.wallet.billing.amazonPay.usecases


import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import javax.inject.Inject

class SaveAmazonPayPaymentRedirectTypeUseCase @Inject constructor(
  private val amazonPayRepository: AmazonPayRepository,
) {

  operator fun invoke(paymentType: String?) =
    amazonPayRepository.saveAmazonPayPaymentType(paymentType)
}
