package com.asfoundation.wallet.verification

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Completable
import io.reactivex.Single

class WalletVerificationInteractor(private val verificationRepository: VerificationRepository,
                                   private val adyenPaymentRepository: AdyenPaymentRepository,
                                   private val walletService: WalletService) {

  fun isCurrentWalletVerified(): Single<Boolean> {
    return getCurrentWalletVerificationStatus()
        .map { status -> status == VerificationStatus.VERIFIED }
  }

  fun isVerified(address: String, signature: String): Single<Boolean> {
    return getVerificationStatus(address, signature)
        .map { status -> status == VerificationStatus.VERIFIED }
  }

  fun getCurrentWalletVerificationStatus(): Single<VerificationStatus> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          getVerificationStatus(walletAddressModel.address, walletAddressModel.signedAddress)
        }
  }

  fun getVerificationStatus(address: String, signature: String): Single<VerificationStatus> {
    return verificationRepository.getVerificationStatus(address, signature)
  }

  fun getCachedVerificationStatus(address: String): VerificationStatus {
    return verificationRepository.getCachedValidationStatus(address)
  }

  fun removeWalletVerificationStatus(address: String): Completable {
    return verificationRepository.removeCachedWalletValidationStatus(address)
  }

  fun makeVerificationPayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                              returnUrl: String): Single<VerificationPaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          adyenPaymentRepository.makeVerificationPayment(adyenPaymentMethod, shouldStoreMethod,
              returnUrl, addressModel.address, addressModel.signedAddress)
              .doOnSuccess { paymentModel ->
                if (paymentModel.success) {
                  verificationRepository.saveVerificationStatus(addressModel.address,
                      VerificationStatus.CODE_REQUESTED)
                }
              }
        }

  }
}