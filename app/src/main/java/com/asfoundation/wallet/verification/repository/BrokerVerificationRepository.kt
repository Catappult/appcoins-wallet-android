package com.asfoundation.wallet.verification.repository

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.*
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.VerificationInfoResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationPayment
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.appcoins.wallet.sharedpreferences.BrokerVerificationPreferencesDataSource
import javax.inject.Inject

class BrokerVerificationRepository @Inject constructor(
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val sharedPreferences: BrokerVerificationPreferencesDataSource
) {

  fun getVerificationInfo(
    walletAddress: String,
    signedWalletAddress: String
  ): Single<VerificationInfoResponse> {
    return brokerVerificationApi.getVerificationInfo(walletAddress, signedWalletAddress)
  }

  fun makeCreditCardVerificationPayment(
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
    returnUrl: String, walletAddress: String,
    walletSignature: String
  ): Single<VerificationPaymentModel> {
    return brokerVerificationApi.makeCreditCardVerificationPayment(
      walletAddress, walletSignature,
      VerificationPayment(
        adyenPaymentMethod, shouldStoreMethod,
        returnUrl
      )
    )
        .toSingle { adyenResponseMapper.mapVerificationPaymentModelSuccess() }
        .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun makePaypalVerificationPayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                                    returnUrl: String, walletAddress: String,
                                    walletSignature: String): Single<VerificationPaymentModel> {
    return brokerVerificationApi.makePaypalVerificationPayment(walletAddress, walletSignature,
      VerificationPayment(
        adyenPaymentMethod, shouldStoreMethod,
        returnUrl
      ))
        .map { adyenResponseMapper.mapVerificationPaymentModelSuccess(it) }
        .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun validateCode(code: String, walletAddress: String,
                   walletSignature: String): Single<VerificationCodeResult> {
    return brokerVerificationApi.validateCode(walletAddress, walletSignature, code)
        .toSingle { VerificationCodeResult(true) }
        .onErrorReturn { adyenResponseMapper.mapVerificationCodeError(it) }
  }

  fun getVerificationStatus(walletAddress: String,
                            walletSignature: String): Single<VerificationStatus> {
    return walletInfoRepository.getLatestWalletInfo(walletAddress, updateFiatValues = false)
        .subscribeOn(Schedulers.io())
        .flatMap { walletInfo ->
          if (walletInfo.verified) {
            return@flatMap Single.just(VerificationStatus.VERIFIED)
          } else {
            if (getCachedValidationStatus(walletAddress) == VerificationStatus.VERIFYING) {
              return@flatMap Single.just(VerificationStatus.VERIFYING)
            }
            return@flatMap getCardVerificationState(walletAddress, walletSignature)
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
    return brokerVerificationApi.getVerificationState(walletAddress, walletSignature)
      .map { verificationState ->
        if (verificationState == "ACTIVE") VerificationStatus.CODE_REQUESTED
        else VerificationStatus.UNVERIFIED
      }
      .onErrorReturn {
        if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK
        else VerificationStatus.ERROR
      }
  }

  fun saveVerificationStatus(walletAddress: String, status: VerificationStatus) =
    sharedPreferences.saveVerificationStatus(walletAddress, status.ordinal)


  fun getCachedValidationStatus(walletAddress: String) =
    VerificationStatus.values()[sharedPreferences.getCachedValidationStatus(walletAddress)]

  fun removeCachedWalletValidationStatus(walletAddress: String): Completable {
    return Completable.fromAction {
      sharedPreferences.removeCachedWalletValidationStatus(walletAddress)
    }
  }
}