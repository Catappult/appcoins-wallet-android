package com.asfoundation.wallet.verification

import com.appcoins.wallet.bdsbilling.WalletService
import io.reactivex.Single

class WalletVerificationActivityInteractor(
    private val walletVerificationRepository: WalletVerificationRepository,
    private val walletService: WalletService
) {

  fun getVerificationStatus(): Single<WalletVerificationStatus> {
    return walletService.getWalletAddress()
        .flatMap { walletVerificationRepository.getVerificationStatus(it) }
        .onErrorReturn { WalletVerificationStatus.UNVERIFIED }
  }

}