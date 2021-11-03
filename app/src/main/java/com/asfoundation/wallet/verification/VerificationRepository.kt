package com.asfoundation.wallet.verification

import android.content.SharedPreferences
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.verification.network.VerificationApi
import com.asfoundation.wallet.verification.network.VerificationStateApi
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class VerificationRepository(private val verificationApi: VerificationApi,
                             private val verificationStateApi: VerificationStateApi,
                             private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
  }

  fun getVerificationStatus(walletAddress: String,
                            walletSignature: String): Single<VerificationStatus> {
    return verificationApi.isUserVerified(walletAddress)
        .subscribeOn(Schedulers.io())
        .flatMap { verificationResponse ->
          if (verificationResponse.verified) {
            Single.just(VerificationStatus.VERIFIED)
          } else {
            getCardVerificationState(walletAddress, walletSignature)
          }
        }
        .doOnSuccess { status -> saveVerificationStatus(walletAddress, status) }
        .onErrorReturn {
          if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK
          else VerificationStatus.ERROR
        }
  }

  fun getCardVerificationState(walletAddress: String,
                               walletSignature: String): Single<VerificationStatus> {
    return verificationStateApi.getVerificationState(walletAddress, walletSignature)
        .map { verificationState ->
          if (verificationState == "ACTIVE") VerificationStatus.CODE_REQUESTED
          else VerificationStatus.UNVERIFIED
        }
        .onErrorReturn {
          if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK
          else VerificationStatus.ERROR
        }
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