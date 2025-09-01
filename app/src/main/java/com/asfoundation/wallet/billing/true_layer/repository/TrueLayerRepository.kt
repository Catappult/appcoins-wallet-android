package com.asfoundation.wallet.billing.true_layer.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.broker.TrueLayerApi
import com.appcoins.wallet.core.network.microservices.model.TrueLayerPayment
import com.appcoins.wallet.core.network.microservices.model.TrueLayerResponse
import com.appcoins.wallet.core.network.microservices.model.TrueLayerTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class TrueLayerRepository @Inject constructor(
  private val trueLayerApi: TrueLayerApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val adyenResponseMapper: AdyenResponseMapper,
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
    method: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
  ): Single<TrueLayerTransaction> =
    trueLayerApi.createTransaction(
      walletAddress = walletAddress,
      trueLayerPayment = TrueLayerPayment(
        callbackUrl = callbackUrl,
        domain = packageName,
        metadata = metadata,
        origin = origin,
        method = method,
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
      )
    )
      .subscribeOn(rxSchedulers.io)
      .map { response: TrueLayerResponse ->
        TrueLayerTransaction(
          uid = response.uid,
          hash = response.hash,
          status = response.status,
          validity = response.mapValidity(),
          paymentId = response.paymentId,
          resourceToken = response.resourceToken
        )
      }
      .onErrorReturn {
        val httpException = (it as? HttpException)
        val errorCode = httpException?.code()
        val errorContent = httpException?.response()?.errorBody()?.string()
        handleCreateTransactionErrorCodes(errorCode, errorContent)
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
        logger.log("TrueLayerRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  private fun handleCreateTransactionErrorCodes(
    errorCode: Int?, errorContent: String?
  ): TrueLayerTransaction {
    val validity = when (errorCode) {
      else -> TrueLayerTransaction.TrueLayerValidityState.ERROR
    }
    return TrueLayerTransaction(
      uid = null,
      hash = null,
      status = null,
      validity = validity,
      paymentId = errorCode.toString(),
      resourceToken = errorContent ?: ""
    )
  }
}
