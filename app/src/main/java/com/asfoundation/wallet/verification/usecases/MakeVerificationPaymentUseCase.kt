package com.asfoundation.wallet.verification.usecases

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.Single
import javax.inject.Inject

class MakeVerificationPaymentUseCase @Inject constructor(
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val walletService: WalletService) {

  operator fun invoke(verificationType: WalletVerificationInteractor.VerificationType,
                      adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                      returnUrl: String): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          when (verificationType) {
            WalletVerificationInteractor.VerificationType.PAYPAL -> {
              brokerVerificationRepository.makePaypalVerificationPayment(adyenPaymentMethod,
                  shouldStoreMethod, returnUrl, addressModel.address, addressModel.signedAddress)
            }
            WalletVerificationInteractor.VerificationType.CREDIT_CARD -> {
              brokerVerificationRepository.makeCreditCardVerificationPayment(adyenPaymentMethod,
                  shouldStoreMethod, returnUrl, addressModel.address, addressModel.signedAddress)
                  .doOnSuccess { paymentModel ->
                    if (paymentModel.success) {
                      brokerVerificationRepository.saveVerificationStatus(addressModel.address,
                          VerificationStatus.CODE_REQUESTED)
                    }
                  }
            }
          }
        }
  }
}