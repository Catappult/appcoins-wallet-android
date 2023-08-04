package com.asfoundation.wallet.verification.ui.credit_card

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletVerificationInteractor @Inject constructor(
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val walletService: WalletService) {

  enum class VerificationType { PAYPAL, CREDIT_CARD }

  fun isVerified(address: String, signature: String): Single<Boolean> {
    return getVerificationStatus(address, signature)
        .map { status -> status == VerificationStatus.VERIFIED }
  }

  private fun getVerificationStatus(address: String,
                                    signature: String): Single<VerificationStatus> {
    return brokerVerificationRepository.getVerificationStatus(address, signature)
  }

  fun getCachedVerificationStatus(address: String): VerificationStatus {
    return brokerVerificationRepository.getCachedValidationStatus(address)
  }

  fun removeWalletVerificationStatus(address: String): Completable {
    return brokerVerificationRepository.removeCachedWalletValidationStatus(address)
  }

  internal fun makeVerificationPayment(verificationType: VerificationType,
                                       adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                                       returnUrl: String): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          when (verificationType) {
            VerificationType.PAYPAL -> {
              brokerVerificationRepository.makePaypalVerificationPayment(adyenPaymentMethod,
                  shouldStoreMethod, returnUrl, addressModel.address, addressModel.signedAddress)
            }
            VerificationType.CREDIT_CARD -> {
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

  internal fun confirmVerificationCode(code: String): Single<VerificationCodeResult> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          brokerVerificationRepository.validateCode(code, addressModel.address,
              addressModel.signedAddress)
              .doOnSuccess { result ->
                if (result.success) {
                  brokerVerificationRepository.saveVerificationStatus(addressModel.address,
                      VerificationStatus.VERIFIED)
                }
              }
        }
  }
}