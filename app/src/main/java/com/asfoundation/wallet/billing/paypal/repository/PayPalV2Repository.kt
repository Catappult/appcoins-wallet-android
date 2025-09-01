package com.asfoundation.wallet.billing.paypal.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.broker.PaypalV2Api
import com.appcoins.wallet.core.network.microservices.model.CreateTokenRequest
import com.appcoins.wallet.core.network.microservices.model.PaypalPayment
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import com.appcoins.wallet.core.network.microservices.model.PaypalV2StartResponse
import com.appcoins.wallet.core.network.microservices.model.Urls
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateAgreement
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateToken
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class PayPalV2Repository @Inject constructor(
  private val paypalV2Api: PaypalV2Api,
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
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    guestWalletId: String?
  ): Single<PaypalTransaction> =
    paypalV2Api.createTransaction(
      // uncomment for testing errors in dev
      //HeaderPaypalMock(MockCodes.INSTRUMENT_DECLINED .name).toJson(),
      walletAddress = walletAddress,
      paypalPayment = PaypalPayment(
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
        guestWalletId = guestWalletId
      )
    )
      .subscribeOn(rxSchedulers.io)
      .map { response: PaypalV2StartResponse ->
        PaypalTransaction(
          uid = response.uid,
          hash = response.hash,
          status = response.status,
          validity = response.mapValidity()
        )
      }
      .onErrorReturn {
        val httpException = (it as? HttpException)
        val errorCode = httpException?.code()
        val errorContent = httpException?.response()?.errorBody()?.string()
        handleCreateTransactionErrorCodes(errorCode, errorContent)
      }

  fun createToken(
    walletAddress: String,
    returnUrl: String,
    cancelUrl: String
  ): Single<PaypalCreateToken> =
    paypalV2Api.createToken(
      walletAddress = walletAddress,
      createTokenRequest = CreateTokenRequest(
        Urls(
          returnUrl = returnUrl,
          cancelUrl = cancelUrl
        )
      )
    )
      .subscribeOn(rxSchedulers.io)
      .map { PaypalCreateToken.map(it) }

  fun createBillingAgreement(
    walletAddress: String,
    token: String
  ): Single<PaypalCreateAgreement> =
    paypalV2Api.createBillingAgreement(
      walletAddress = walletAddress,
      token = token
    )
      .subscribeOn(rxSchedulers.io)
      .map { PaypalCreateAgreement.map(it) }

  fun cancelToken(
    walletAddress: String,
    token: String
  ): Completable =
    paypalV2Api.cancelToken(
      walletAddress = walletAddress,
      token = token
    )
      .subscribeOn(rxSchedulers.io)
      .ignoreElement()

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
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  fun getCurrentBillingAgreement(
    walletAddress: String,
  ): Single<Boolean> =
    paypalV2Api.getCurrentBillingAgreement(
      walletAddress = walletAddress,
    )
      .subscribeOn(rxSchedulers.io)
      .map { it.uid.isNotEmpty() }
      .onErrorReturn { false }

  fun removeBillingAgreement(
    walletAddress: String
  ): Completable =
    paypalV2Api.removeBillingAgreement(
      walletAddress = walletAddress,
    )
      .subscribeOn(rxSchedulers.io)
      .ignoreElement()

  private fun handleCreateTransactionErrorCodes(
    errorCode: Int?,
    errorContent: String?
  ): PaypalTransaction {
    val validity = when (errorCode) {
      404 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      400 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      else -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      // If the payment fails with a previous billing agreement, then always tries to login
      // again once
    }
    return PaypalTransaction(
      uid = null,
      hash = null,
      status = null,
      validity = validity,
      errorCode = errorCode.toString(),
      errorMessage = errorContent ?: ""
    )
  }

}