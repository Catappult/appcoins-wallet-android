package com.asfoundation.wallet.verification.repository

import android.content.SharedPreferences
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.*
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject

class BrokerVerificationRepository @Inject constructor(
    private val walletInfoRepository: WalletInfoRepository,
    private val brokerVerificationApi: BrokerVerificationApi,
    private val adyenResponseMapper: AdyenResponseMapper,
    private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
  }

  fun getVerificationInfo(walletAddress: String,
                          signedWalletAddress: String): Single<VerificationInfoResponse> {
    return brokerVerificationApi.getVerificationInfo(walletAddress, signedWalletAddress)
  }

  fun makeCreditCardVerificationPayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                                        returnUrl: String, walletAddress: String,
                                        walletSignature: String): Single<VerificationPaymentModel> {
    return brokerVerificationApi.makeCreditCardVerificationPayment(walletAddress, walletSignature,
        AdyenPaymentRepository.VerificationPayment(adyenPaymentMethod, shouldStoreMethod,
            returnUrl))
        .toSingle { adyenResponseMapper.mapVerificationPaymentModelSuccess() }
        .onErrorReturn { adyenResponseMapper.mapVerificationPaymentModelError(it) }
  }

  fun makePaypalVerificationPayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                                    returnUrl: String, walletAddress: String,
                                    walletSignature: String): Single<VerificationPaymentModel> {
    return brokerVerificationApi.makePaypalVerificationPayment(walletAddress, walletSignature,
        AdyenPaymentRepository.VerificationPayment(adyenPaymentMethod, shouldStoreMethod,
            returnUrl))
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

  interface BrokerVerificationApi {

    @GET("8.20200815/gateways/adyen_v2/verification/state")
    fun getVerificationState(@Query("wallet.address") wallet: String,
                             @Query("wallet.signature") walletSignature: String): Single<String>

    @GET("8.20200815/gateways/adyen_v2/verification/info")
    fun getVerificationInfo(@Query("wallet.address") walletAddress: String,
                            @Query("wallet.signature")
                            walletSignature: String): Single<VerificationInfoResponse>

    @POST("8.20200815/gateways/adyen_v2/verification/generate")
    fun makePaypalVerificationPayment(@Query("wallet.address") walletAddress: String,
                                      @Query("wallet.signature") walletSignature: String,
                                      @Body
                                      verificationPayment: AdyenPaymentRepository.VerificationPayment): Single<AdyenTransactionResponse>

    @POST("8.20200815/gateways/adyen_v2/verification/generate")
    fun makeCreditCardVerificationPayment(@Query("wallet.address") walletAddress: String,
                                          @Query("wallet.signature") walletSignature: String,
                                          @Body
                                          verificationPayment: AdyenPaymentRepository.VerificationPayment): Completable

    @POST("8.20200815/gateways/adyen_v2/verification/validate")
    fun validateCode(@Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Body code: String): Completable
  }
}