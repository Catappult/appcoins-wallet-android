package com.asfoundation.wallet.verification.usecases

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import io.reactivex.Single
import javax.inject.Inject

class MakeVerificationPaymentUseCase @Inject constructor(
    private val brokerVerificationRepository: com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository,
    private val walletService: WalletService) {

  operator fun invoke(verificationType: com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor.VerificationType,
                      adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                      returnUrl: String): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          when (verificationType) {
            com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor.VerificationType.PAYPAL -> {
              brokerVerificationRepository.makePaypalVerificationPayment(adyenPaymentMethod,
                  shouldStoreMethod, returnUrl, addressModel.address, addressModel.signedAddress)
            }
            com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor.VerificationType.CREDIT_CARD -> {
              brokerVerificationRepository.makeCreditCardVerificationPayment(adyenPaymentMethod,
                  shouldStoreMethod, returnUrl, addressModel.address, addressModel.signedAddress)
                  .doOnSuccess { paymentModel ->
                    if (paymentModel.success) {
                      brokerVerificationRepository.saveVerificationStatus(addressModel.address,
                          com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.CODE_REQUESTED)
                    }
                  }
            }
          }
        }
  }
}