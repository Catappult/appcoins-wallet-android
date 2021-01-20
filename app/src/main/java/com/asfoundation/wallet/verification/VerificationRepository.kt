package com.asfoundation.wallet.verification

import android.content.SharedPreferences
import com.asfoundation.wallet.verification.network.ValidationApi
import com.asfoundation.wallet.verification.network.VerificationApi
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Completable
import io.reactivex.Single

class VerificationRepository(private val verificationApi: VerificationApi,
                             private val validationApi: ValidationApi,
                             private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
  }

  fun getVerificationStatus(walletAddress: String,
                            walletSignature: String): Single<VerificationStatus> {
    return verificationApi.isUserVerified(walletAddress)
        .flatMap { verificationResponse ->
          if (verificationResponse.verified) {
            Single.just(VerificationStatus.VERIFIED)
          } else {
            validationApi.getValidationState(walletAddress, walletSignature)
                .map { validationState ->
                  if (validationState == "ACTIVE")
                    VerificationStatus.CODE_REQUESTED
                  else
                    VerificationStatus.UNVERIFIED
                }
          }
        }
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