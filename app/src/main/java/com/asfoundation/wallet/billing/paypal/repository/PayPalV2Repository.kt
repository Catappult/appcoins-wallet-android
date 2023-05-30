package com.asfoundation.wallet.billing.paypal.repository

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.network.microservices.api.broker.PaypalV2Api
import com.appcoins.wallet.core.network.microservices.model.*
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateAgreement
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateToken
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class PayPalV2Repository @Inject constructor(
  private val paypalV2Api: PaypalV2Api,
  private val brokerBdsApi: BrokerBdsApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val logger: Logger
) {

  fun createTransaction(
    value: String,
    currency: String, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String, developerWallet: String?,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?,
    walletSignature: String,
    referrerUrl: String?
  ): Single<PaypalTransaction> {
    return paypalV2Api.createTransaction(
      // uncomment for testing errors in dev
      //HeaderPaypalMock(MockCodes.INSTRUMENT_DECLINED .name).toJson(),
      walletAddress,
      walletSignature,
      PaypalPayment(
        callbackUrl = callbackUrl,
        domain = packageName,
        metadata = metadata,
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
    )
      .map { response: PaypalV2StartResponse ->
        PaypalTransaction(
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

  fun createToken(
    walletAddress: String,
    walletSignature: String,
    returnUrl: String,
    cancelUrl: String
  ): Single<PaypalCreateToken> {
    return paypalV2Api.createToken(
      walletAddress,
      walletSignature,
      CreateTokenRequest(
        Urls(
          returnUrl = returnUrl,
          cancelUrl = cancelUrl
        )
      )
    )
      .map { response: PaypalV2CreateTokenResponse ->
        PaypalCreateToken.map(response)
      }
  }

  fun createBillingAgreement(
    walletAddress: String,
    walletSignature: String,
    token: String
  ): Single<PaypalCreateAgreement> {
    return paypalV2Api.createBillingAgreement(
      walletAddress,
      walletSignature,
      token = token
    )
      .map { response: PaypalV2CreateAgreementResponse ->
        PaypalCreateAgreement.map(response)
      }
  }

  fun cancelToken(
    walletAddress: String,
    walletSignature: String,
    token: String
  ): Completable {
    return paypalV2Api.cancelToken(
      walletAddress,
      walletSignature,
      token = token
    )
      .ignoreElement()
  }

  fun getTransaction(
    uid: String, walletAddress: String,
    signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(uid, walletAddress, signedWalletAddress)
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  fun getCurrentBillingAgreement(
    walletAddress: String,
    walletSignature: String
  ): Single<Boolean> {
    return paypalV2Api.getCurrentBillingAgreement(
      walletAddress,
      walletSignature
    )
      .map { response: PaypalV2GetAgreementResponse ->
        response.uid.isNotEmpty()
      }
      .onErrorReturn { false }
  }

  fun removeBillingAgreement(
    walletAddress: String,
    walletSignature: String
  ): Completable {
    return paypalV2Api.removeBillingAgreement(
      walletAddress,
      walletSignature
    )
      .ignoreElement()
  }

  private fun handleCreateTransactionErrorCodes(errorCode: Int?, errorContent: String?): PaypalTransaction {
    val validity = when (errorCode) {
      404 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      400 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      else -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      // If the payment fails with a previous billing agreement, then always tries to login
      // again once
//      else -> PaypalTransaction.PaypalValidityState.ERROR
    }
    return PaypalTransaction(
      null,
      null,
      null,
      validity,
      errorCode.toString(),
      errorContent ?: ""
    )
  }

}