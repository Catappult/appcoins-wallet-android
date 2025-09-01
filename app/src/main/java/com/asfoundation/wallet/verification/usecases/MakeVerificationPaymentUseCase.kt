package com.asfoundation.wallet.verification.usecases

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import io.reactivex.Single
import javax.inject.Inject

class MakeVerificationPaymentUseCase @Inject constructor(
  private val brokerVerificationRepository: com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository,
  private val walletService: WalletService
) {

  operator fun invoke(
    verificationType: VerificationType,
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
    returnUrl: String
  ): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { addressModel ->
        when (verificationType) {
          VerificationType.PAYPAL -> {
            brokerVerificationRepository.makePaypalVerificationPayment(
              adyenPaymentMethod = adyenPaymentMethod,
              shouldStoreMethod = shouldStoreMethod,
              returnUrl = returnUrl,
              walletAddress = addressModel.address
            )
          }

          VerificationType.CREDIT_CARD -> {
            brokerVerificationRepository.makeCreditCardVerificationPayment(
              adyenPaymentMethod = adyenPaymentMethod,
              shouldStoreMethod = shouldStoreMethod,
              returnUrl = returnUrl,
              walletAddress = addressModel.address
            )
              .doOnSuccess { paymentModel ->
                if (paymentModel.success) {
                  brokerVerificationRepository.saveVerificationStatus(
                    addressModel.address,
                    com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.CODE_REQUESTED,
                    verificationType
                  )
                }
              }
          }
        }
      }
  }
}