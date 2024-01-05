package com.asfoundation.wallet.billing.googlepay.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenSessionApi
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.model.*
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class GooglePayWebRepository @Inject constructor(
  private val adyenSessionbApi: AdyenSessionApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val logger: Logger,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,
) {

  fun createTransaction(
    value: String,
    currency: String,
    reference: String?,
    walletAddress: String,
    origin: String?,
    packageName: String?,
    metadata: String?,
    method: String,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    developerWallet: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?
  ): Single<GooglePayWebTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io).flatMap { ewt ->
      adyenSessionbApi.createSessionTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          sessionPaymentDetails = SessionPaymentDetails(
            callbackUrl = callbackUrl,
            domain = packageName,
            metadata = metadata,
            method = method,
            origin = origin,
            sku = sku,
            reference = reference,
            type = transactionType,
            currency = currency,
            value = value,
            developer = developerWallet,
            entityOemId = entityOemId,
            entityDomain = entityDomain,
            entityPromoCode = entityPromoCode,
            user = userWallet,
            referrerUrl = referrerUrl
          )
        ).map { response: AdyenSessionResponse ->
            GooglePayWebTransaction(
              response.uid, response.hash, response.status, response.mapValidity()
            )
          }.onErrorReturn {
            val httpException = (it as? HttpException)
            val errorCode = httpException?.code()
            val errorContent = httpException?.response()?.errorBody()?.string()
            handleCreateTransactionErrorCodes(errorCode, errorContent)
          }
      }
  }

  fun getTransaction(  //TODO new error mapping
    uid: String, walletAddress: String, signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(
      uId = uid, walletAddress = walletAddress, walletSignature = signedWalletAddress
    ).map { adyenResponseMapper.map(it) }.onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  private fun handleCreateTransactionErrorCodes(  // TODO new error mapping
    errorCode: Int?, errorContent: String?
  ): GooglePayWebTransaction {
    val validity = when (errorCode) {
      404 -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      // TODO check if needed
      else -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
    }
    return GooglePayWebTransaction(
      null, null, null, validity, errorCode.toString(), errorContent ?: ""
    )
  }

}