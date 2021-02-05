package com.asfoundation.wallet.verification

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Single

class VerificationActivityInteractor(
    private val verificationRepository: VerificationRepository,
    private val walletService: WalletService
) {

  fun getVerificationStatus(): Single<VerificationStatus> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          verificationRepository.getVerificationStatus(addressModel.address,
              addressModel.signedAddress)
        }
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }
}