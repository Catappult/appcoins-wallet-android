package com.asfoundation.wallet.billing.googlepay.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenSessionApi
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.model.AdyenSessionResponse
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.network.microservices.model.SessionPaymentDetails
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.sharedpreferences.GooglePayDataSource
import com.asfoundation.wallet.billing.googlepay.models.GooglePayUrls
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class GooglePayWebRepository @Inject constructor(
  private val adyenSessionbApi: AdyenSessionApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val googlePayDataSource: GooglePayDataSource,
  private val logger: Logger,
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
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    returnUrl: String,
    guestWalletId: String?
  ): Single<GooglePayWebTransaction> =
    adyenSessionbApi.createSessionTransaction(
      walletAddress = walletAddress,
      sessionPaymentDetails = SessionPaymentDetails(
        returnUrl = returnUrl,
        channel = "ANDROID",
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
        entityOemId = entityOemId,
        entityDomain = entityDomain,
        entityPromoCode = entityPromoCode,
        user = userWallet,
        referrerUrl = referrerUrl,
        guestWalletId = guestWalletId
      )
    )
      .subscribeOn(rxSchedulers.io)
      .map { response: AdyenSessionResponse ->
        with(response) {
          GooglePayWebTransaction(
            uid = uid,
            hash = hash,
            status = status,
            validity = mapValidity(),
            sessionId = session?.id,
            sessionData = session?.sessionData,
          )
        }
      }.onErrorReturn {
        val httpException = (it as? HttpException)
        val errorCode = httpException?.code()
        val errorContent = httpException?.response()?.errorBody()?.string()
        handleCreateTransactionErrorCodes(errorCode, errorContent)
      }

  fun getGooglePayUrl(): Single<GooglePayUrls> {
    return brokerBdsApi.getGooglePayUrls().map {
      GooglePayUrls(it.url, it.returnUrl)
    }
  }

  fun getTransaction(
    uid: String,
    walletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(
      uId = uid,
      walletAddress = walletAddress
    )
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("GooglePayRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  private fun handleCreateTransactionErrorCodes(
    errorCode: Int?,
    errorContent: String?
  ): GooglePayWebTransaction {
    val validity = when (errorCode) {
      404 -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      else -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
    }
    return GooglePayWebTransaction(
      uid = null,
      hash = null,
      status = null,
      validity = validity,
      sessionId = errorCode.toString(),
      sessionData = errorContent ?: ""
    )
  }

  fun saveChromeResult(result: String) {
    googlePayDataSource.saveResult(result)
  }

  fun consumeChromeResult(): String {
    return googlePayDataSource.consumeResult()
  }

}
