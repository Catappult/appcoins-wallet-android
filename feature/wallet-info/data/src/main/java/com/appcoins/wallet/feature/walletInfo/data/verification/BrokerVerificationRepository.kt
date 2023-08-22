package com.appcoins.wallet.feature.walletInfo.data.verification

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationPayment
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import com.appcoins.wallet.sharedpreferences.BrokerVerificationPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BrokerVerificationRepository @Inject constructor(
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val sharedPreferences: BrokerVerificationPreferencesDataSource,
  private val ewtObtainer: com.appcoins.wallet.core.network.base.EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,
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
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      verificationPayment = VerificationPayment(
        adyenPaymentMethod, shouldStoreMethod,
        returnUrl
      )
    )
      .toSingle { adyenResponseMapper.mapVerificationPaymentModelSuccess() }
      .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun makePaypalVerificationPayment(
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
    returnUrl: String, walletAddress: String,
    walletSignature: String
  ): Single<VerificationPaymentModel> {
    return brokerVerificationApi.makePaypalVerificationPayment(
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      verificationPayment = VerificationPayment(
        adyenPaymentMethod, shouldStoreMethod,
        returnUrl
      )
    )
      .map { adyenResponseMapper.mapVerificationPaymentModelSuccess(it) }
      .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun validateCode(
    code: String, walletAddress: String,
    walletSignature: String
  ): Single<VerificationCodeResult> {
    return brokerVerificationApi.validateCode(
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      code = code
    )
      .toSingle { VerificationCodeResult(true) }
      .onErrorReturn { adyenResponseMapper.mapVerificationCodeError(it) }
  }

  fun getVerificationStatus(
    walletAddress: String,
    walletSignature: String
  ): Single<VerificationStatus> {
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

  fun getCardVerificationState(
    walletAddress: String,
    walletSignature: String
  ): Single<VerificationStatus> {
    return brokerVerificationApi.getVerificationState(
      wallet = walletAddress,
      walletSignature = walletSignature
    )
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