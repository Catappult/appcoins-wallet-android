package com.asfoundation.wallet.verification

import android.content.SharedPreferences
import com.asfoundation.wallet.verification.network.ValidationApi
import com.asfoundation.wallet.verification.network.VerificationApi
import com.asfoundation.wallet.verification.network.VerificationResponse
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class VerificationRepository(private val verificationApi: VerificationApi,
                             private val validationApi: ValidationApi,
                             private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
  }

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
        .doOnSuccess { status -> saveVerificationStatus(walletAddress, status) }
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }

  fun saveVerificationStatus(walletAddress: String, status: VerificationStatus) {
    sharedPreferences.edit()
        .putInt(WALLET_VERIFIED + walletAddress, status.ordinal)
        .apply()
  }

  fun getCachedValidationStatus(walletAddress: String) =
      VerificationStatus.values()[sharedPreferences.getInt(WALLET_VERIFIED + walletAddress, 4)]

  fun removeCachedWalletValidationStatus(walletAddress: String): Completable {
    return Completable.fromAction {
      sharedPreferences.edit()
          .remove(WALLET_VERIFIED + walletAddress)
          .apply()
    }
  }


}