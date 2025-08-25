package com.asfoundation.wallet.billing.sandbox.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.broker.SandboxApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.SandboxPayment
import com.appcoins.wallet.core.network.microservices.model.SandboxTokenPayment
import com.appcoins.wallet.core.network.microservices.model.SandboxResponse
import com.appcoins.wallet.core.network.microservices.model.SandboxTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class SandboxRepository @Inject constructor(
  private val sandboxApi: SandboxApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val subscriptionsApi: SubscriptionBillingApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val logger: Logger,
  private val rxSchedulers: RxSchedulers,
) {

  fun createTransaction(
    value: String,
    currency: String,
    reference: String?,
    walletAddress: String,
    walletSignature: String,
    origin: String?,
    packageName: String?,
    metadata: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    guestWalletId: String?,
  ): Single<SandboxTransaction> {    val responseSingle: Single<SandboxResponse> =
    if (transactionType == BillingSupportedType.INAPP_SUBSCRIPTION.name &&
      packageName != null && sku != null) {
      subscriptionsApi.getSkuSubscriptionToken(
        domain = packageName,
        sku = sku,
        currency = currency,
        walletAddress = walletAddress,
        walletSignature = walletSignature,
      )
        .flatMap { token ->
          sandboxApi.createTokenTransaction(
            walletAddress = walletAddress,
            sandboxPayment = SandboxTokenPayment(
              callbackUrl = callbackUrl,
              metadata = metadata,
              origin = origin,
              reference = reference,
              entityOemId = entityOemId,
              entityDomain = entityDomain,
              entityPromoCode = entityPromoCode,
              user = userWallet,
              referrerUrl = referrerUrl,
              guestWalletId = guestWalletId,
              productToken = token,
            )
          )
        }
    } else {
      sandboxApi.createTransaction(
        walletAddress = walletAddress,
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
          referrerUrl = referrerUrl,
          guestWalletId = guestWalletId,
        )
      )
    }

    return responseSingle
      .subscribeOn(rxSchedulers.io)
      .map { response: SandboxResponse ->
        SandboxTransaction(
          uid = response.uid,
          hash = response.hash,
          status = response.status,
          validity = response.mapValidity(),
        )
      }
      .onErrorReturn {
        val httpException = (it as? HttpException)
        val errorCode = httpException?.code()
        val errorContent = httpException?.response()?.errorBody()?.string()
        handleCreateTransactionErrorCodes(errorCode, errorContent)
      }
  }

  fun getTransaction(
    uid: String, walletAddress: String,
    signedWalletAddress: String
  ): Single<PaymentModel> =
    brokerBdsApi.getAppcoinsTransaction(
      uId = uid,
      walletAddress = walletAddress,
      walletSignature = signedWalletAddress
    )
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
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
      uid = null,
      hash = null,
      status = null,
      validity = validity,
      errorCode = errorCode.toString(),
      errorMessage = errorContent ?: ""
    )
  }
}
