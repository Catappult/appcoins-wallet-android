package com.asfoundation.wallet.verification

import com.appcoins.wallet.bdsbilling.WalletService
import io.reactivex.Single

class VerificationActivityInteractor(
    private val verificationRepository: VerificationRepository,
    private val walletService: WalletService
) {

  fun getVerificationStatus(): Single<VerificationStatus> {
    return walletService.getWalletAddress()
        .flatMap { verificationRepository.getVerificationStatus(it) }
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }

}