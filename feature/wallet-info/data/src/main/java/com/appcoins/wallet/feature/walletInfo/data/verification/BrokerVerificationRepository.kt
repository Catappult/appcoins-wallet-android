package com.appcoins.wallet.feature.walletInfo.data.verification

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationPayment
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import com.appcoins.wallet.sharedpreferences.BrokerVerificationPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class BrokerVerificationRepository
@Inject
constructor(
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val sharedPreferences: BrokerVerificationPreferencesDataSource,
) {

  fun getVerificationInfo(
    walletAddress: String
  ): Single<VerificationInfoResponse> {
    return brokerVerificationApi.getVerificationInfo(walletAddress)
  }

  fun makeCreditCardVerificationPayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    returnUrl: String,
    walletAddress: String
  ): Single<VerificationPaymentModel> {
    return brokerVerificationApi
      .makeCreditCardVerificationPayment(
        walletAddress = walletAddress,
        verificationPayment =
        VerificationPayment(adyenPaymentMethod, shouldStoreMethod, returnUrl)
      )
      .toSingle { adyenResponseMapper.mapVerificationPaymentModelSuccess() }
      .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun makePaypalVerificationPayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    returnUrl: String,
    walletAddress: String
  ): Single<VerificationPaymentModel> {
    return brokerVerificationApi
      .makePaypalVerificationPayment(
        walletAddress = walletAddress,
        verificationPayment =
        VerificationPayment(adyenPaymentMethod, shouldStoreMethod, returnUrl)
      )
      .map { adyenResponseMapper.mapVerificationPaymentModelSuccess(it) }
      .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun validateCode(
    code: String,
    walletAddress: String
  ): Single<VerificationCodeResult> {
    return brokerVerificationApi
      .validateCode(walletAddress = walletAddress, code = code)
      .toSingle { VerificationCodeResult(true) }
      .onErrorReturn { adyenResponseMapper.mapVerificationCodeError(it) }
  }

  fun getVerificationStatus(
    walletAddress: String,
    type: VerificationType
  ): Single<VerificationStatus> {
    return walletInfoRepository
      .getLatestWalletInfo(walletAddress)
      .subscribeOn(Schedulers.io())
      .flatMap {
        // CODE_REQUESTED/VERIFYING aren't final: PayPal completes outside the app (email link).
        when (val cached = getCachedValidationStatus(walletAddress, type)) {
          VerificationStatus.VERIFIED,
          VerificationStatus.NO_NETWORK,
          VerificationStatus.ERROR -> Single.just(cached)
          else -> getServerVerificationState(walletAddress, type)
        }
      }
      .doOnSuccess { status -> saveVerificationStatus(walletAddress, status, type) }
      .onErrorReturn {
        if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK else VerificationStatus.ERROR
      }
  }

  fun getServerVerificationState(
    walletAddress: String,
    type: VerificationType,
  ): Single<VerificationStatus> {
    val method = if (type == VerificationType.PAYPAL) "paypal" else "credit_card"
    return brokerVerificationApi
      .getVerificationState(method = method, wallet = walletAddress)
      .map { state ->
        when (state) {
          "VERIFIED" -> VerificationStatus.VERIFIED
          "PENDING_CODE" -> VerificationStatus.CODE_REQUESTED
          "PENDING_VALIDATION" -> VerificationStatus.VERIFYING
          else -> VerificationStatus.UNVERIFIED // CANCELED, EXPIRED, FAILED
        }
      }
      .onErrorReturn {
        val cached = getCachedValidationStatus(walletAddress, type)
        when {
          it is HttpException && it.code() == 404 -> VerificationStatus.UNVERIFIED
          // offline fallback: keep an in-progress verification instead of losing it
          cached == VerificationStatus.CODE_REQUESTED || cached == VerificationStatus.VERIFYING -> cached
          it.isNoNetworkException() -> VerificationStatus.NO_NETWORK
          else -> VerificationStatus.ERROR
        }
      }
  }

  fun saveVerificationStatus(
    walletAddress: String,
    status: VerificationStatus,
    type: VerificationType
  ) =
    sharedPreferences.saveVerificationStatus(walletAddress, status.ordinal, type.ordinal)

  fun getCachedValidationStatus(walletAddress: String, type: VerificationType) =
    VerificationStatus.values()[sharedPreferences.getCachedValidationStatus(
      walletAddress,
      type.ordinal
    )]

  fun removeCachedWalletValidationStatus(
    walletAddress: String,
    type: VerificationType
  ): Completable {
    return Completable.fromAction {
      sharedPreferences.removeCachedWalletValidationStatus(walletAddress, type.ordinal)
    }
  }
}
