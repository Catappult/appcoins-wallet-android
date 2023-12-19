package com.asfoundation.wallet.billing.sandbox.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.broker.SandboxApi
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.network.microservices.model.*
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class SandboxRepository @Inject constructor(
  private val sandboxApi: SandboxApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val logger: Logger,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,

  ) {

  fun createTransaction(
    value: String,
    currency: String, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String, developerWallet: String?,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?
  ): Single<SandboxTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        sandboxApi.createTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          sandboxPayment = SandboxPayment(
            callbackUrl = callbackUrl,
            domain = packageName,
            metadata = metadata,
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
            referrerUrl = referrerUrl
          )
        )
          .map { response: SandboxResponse ->
            SandboxTransaction(
              response.uid,
              response.hash,
              response.status,
              response.mapValidity()
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
    uid: String, walletAddress: String,
    signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(
      uId = uid,
      walletAddress = walletAddress,
      walletSignature = signedWalletAddress
    )
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  private fun handleCreateTransactionErrorCodes(
    errorCode: Int?,
    errorContent: String?
  ): SandboxTransaction {
    val validity = when (errorCode) {
//      404 -> SandboxTransaction.SandboxValidityState.ERROR
      else -> SandboxTransaction.SandboxValidityState.ERROR
    }
    return SandboxTransaction(
      null,
      null,
      null,
      validity,
      errorCode.toString(),
      errorContent ?: ""
    )
  }

}