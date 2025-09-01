package com.appcoins.wallet.feature.walletInfo.data.verification

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletVerificationInteractor
@Inject
constructor(
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val walletService: WalletService
) {

  fun isVerified(address: String, type: VerificationType): Single<Boolean> {
    return getVerificationStatus(address, type).map { status ->
      status == VerificationStatus.VERIFIED
    }
  }

  fun isAtLeastOneVerified(address: String): Single<Boolean> {
    return Single.zip(
      getVerificationStatus(
        address = address,
        type = VerificationType.CREDIT_CARD
      ),
      getVerificationStatus(
        address = address,
        type = VerificationType.PAYPAL
      )
    ) { creditCard, payPal ->
      creditCard == VerificationStatus.VERIFIED || payPal == VerificationStatus.VERIFIED
    }
  }

  fun getVerificationStatus(
    address: String,
    type: VerificationType
  ): Single<VerificationStatus> {
    return brokerVerificationRepository.getVerificationStatus(
      walletAddress = address,
      type = type
    )
  }

  fun getCachedVerificationStatus(address: String, type: VerificationType): VerificationStatus {
    return brokerVerificationRepository.getCachedValidationStatus(address, type)
  }

  fun removeWalletVerificationStatus(address: String, type: VerificationType): Completable {
    return brokerVerificationRepository.removeCachedWalletValidationStatus(address, type)
  }

  fun removeAllWalletVerificationStatus(address: String): Completable {
    brokerVerificationRepository.removeCachedWalletValidationStatus(
      address,
      VerificationType.CREDIT_CARD
    )
    return brokerVerificationRepository.removeCachedWalletValidationStatus(
      address,
      VerificationType.PAYPAL
    )
  }

  fun getCurrentVerificationType(address: String): VerificationType? {
    return when {
      brokerVerificationRepository.getCachedValidationStatus(
        address,
        VerificationType.CREDIT_CARD
      ) == VerificationStatus.CODE_REQUESTED ||
          brokerVerificationRepository.getCachedValidationStatus(
            address,
            VerificationType.CREDIT_CARD
          )
          == VerificationStatus.VERIFYING -> VerificationType.CREDIT_CARD

      brokerVerificationRepository.getCachedValidationStatus(address, VerificationType.PAYPAL)
          == VerificationStatus.CODE_REQUESTED ||
          brokerVerificationRepository.getCachedValidationStatus(address, VerificationType.PAYPAL)
          == VerificationStatus.VERIFYING -> VerificationType.PAYPAL

      else -> null
    }
  }

  fun makeVerificationPayment(
    verificationType: VerificationType,
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    returnUrl: String
  ): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress().flatMap { addressModel ->
      when (verificationType) {
        VerificationType.PAYPAL -> {
          brokerVerificationRepository.makePaypalVerificationPayment(
            adyenPaymentMethod,
            shouldStoreMethod,
            returnUrl,
            addressModel.address
          )
        }

        VerificationType.CREDIT_CARD -> {
          brokerVerificationRepository
            .makeCreditCardVerificationPayment(
              adyenPaymentMethod,
              shouldStoreMethod,
              returnUrl,
              addressModel.address
            )
            .doOnSuccess { paymentModel ->
              if (paymentModel.success) {
                brokerVerificationRepository.saveVerificationStatus(
                  addressModel.address, VerificationStatus.CODE_REQUESTED, verificationType
                )
              }
            }
        }
      }
    }
  }

  fun confirmVerificationCode(
    code: String,
    type: VerificationType
  ): Single<VerificationCodeResult> {
    return walletService.getAndSignCurrentWalletAddress().flatMap { addressModel ->
      brokerVerificationRepository
        .validateCode(
          code = code,
          walletAddress = addressModel.address
        )
        .doOnSuccess { result ->
          if (result.success) {
            brokerVerificationRepository.saveVerificationStatus(
              addressModel.address, VerificationStatus.VERIFIED, type
            )
          }
        }
    }
  }
}
