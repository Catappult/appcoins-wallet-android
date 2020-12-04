package com.asfoundation.wallet.verification

import io.reactivex.Single

class VerificationRepository(private val verificationApi: VerificationApi) {

  fun getVerificationStatus(walletAddress: String): Single<VerificationStatus> {
    return verificationApi.isValid(walletAddress)
        .map { it.status }
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }

}