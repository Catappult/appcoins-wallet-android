package com.asfoundation.wallet.billing.wallet_one.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.broker.WalletOneApi
import com.appcoins.wallet.core.network.microservices.model.Method
import com.appcoins.wallet.core.network.microservices.model.WalletOnePayment
import com.appcoins.wallet.core.network.microservices.model.WalletOneResponse
import com.appcoins.wallet.core.network.microservices.model.WalletOneTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class WalletOneRepository @Inject constructor(
  private val walletOneApi: WalletOneApi,
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
    method: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    successUrl: String,
    failUrl: String
  ): Single<WalletOneTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        walletOneApi.createTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          walletOnePayment = WalletOnePayment(
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
            successUrl = successUrl,
            failUrl = failUrl
          )
        )
          .map { response: WalletOneResponse ->
            WalletOneTransaction(
              response.uid,
              response.hash,
              response.status,
              response.mapValidity(),
              response.htmlData
            )
          }
          .onErrorReturn {
            val httpException = (it as? HttpException)
            val errorCode = httpException?.code()
            val errorContent = httpException?.response()?.errorBody()?.string()
            handleCreateTransactionErrorCodes(errorCode, errorContent)
          }
      }
  }

  fun getTransaction(
    uid: String, walletAddress: String, signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(
      uId = uid, walletAddress = walletAddress, walletSignature = signedWalletAddress
    ).map { adyenResponseMapper.map(it) }.onErrorReturn {
      logger.log("WalletOneRepository", it)
      adyenResponseMapper.mapPaymentModelError(it)
    }
  }

  private fun handleCreateTransactionErrorCodes(
    errorCode: Int?, errorContent: String?
  ): WalletOneTransaction {
    val validity = when (errorCode) {
      else -> WalletOneTransaction.WalletOneValidityState.ERROR
    }
    return WalletOneTransaction(
      null, null, null, validity, errorCode.toString(), errorContent ?: ""
    )
  }


}
