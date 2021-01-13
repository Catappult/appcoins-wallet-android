package com.asfoundation.wallet.verification

import com.asfoundation.wallet.verification.network.ValidationApi
import com.asfoundation.wallet.verification.network.VerificationApi
import com.asfoundation.wallet.verification.network.VerificationResponse
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class VerificationRepository(private val verificationApi: VerificationApi,
                             private val validationApi: ValidationApi) {

  fun getVerificationStatus(walletAddress: String,
                            walletSignature: String): Single<VerificationStatus> {
    return Single.zip(verificationApi.isUserVerified(walletAddress),
        validationApi.getValidationState(walletAddress, walletSignature),
        BiFunction { verified: VerificationResponse, validationState: String ->
          if (verified.verified) {
            return@BiFunction VerificationStatus.VERIFIED
          } else {
            if (validationState == "ACTIVE") {
              return@BiFunction VerificationStatus.CODE_REQUESTED
            }
          }
          return@BiFunction VerificationStatus.UNVERIFIED
        })
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }

}