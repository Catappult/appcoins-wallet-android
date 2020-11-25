package com.asfoundation.wallet.verification

import io.reactivex.Single

class WalletVerificationRepository(private val verificationApi: WalletVerificationApi) {

  fun getVerificationStatus(walletAddress: String): Single<WalletVerificationStatus> {
    return verificationApi.isValid(walletAddress)
        .map { it.status }
        .onErrorReturn { WalletVerificationStatus.UNVERIFIED }
  }

}