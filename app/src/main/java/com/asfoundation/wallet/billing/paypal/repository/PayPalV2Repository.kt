package com.asfoundation.wallet.billing.paypal.repository

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateAgreement
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateToken
import com.asfoundation.wallet.billing.paypal.models.PaypalTransaction
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.http.*
import javax.inject.Inject

class PayPalV2Repository @Inject constructor(
  private val paypalV2Api: PaypalV2Api,
  private val brokerBdsApi: RemoteRepository.BrokerBdsApi,
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
        val errorCode = (it as? HttpException)?.code()
        handleCreateTransactionErrorCodes(errorCode)
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

  fun getTransaction(uid: String, walletAddress: String,
                     signedWalletAddress: String): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(uid, walletAddress, signedWalletAddress)
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  private fun handleCreateTransactionErrorCodes(errorCode: Int?): PaypalTransaction {
    val validity = when (errorCode) {
      404 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      else -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
      // Until all payment errors are treated, if the payment fails with a previous billing
      // agreement, then alway tries to login again once
//      else -> PaypalTransaction.PaypalValidityState.ERROR
    }
    return PaypalTransaction(
      null,
      null,
      null,
      validity
    )
  }

  interface PaypalV2Api {

    @POST("8.20200815/gateways/paypal/transactions")
    fun createTransaction(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body paypalPayment: PaypalPayment
    ): Single<PaypalV2StartResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/token/create")
    fun createToken(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body createTokenRequest: CreateTokenRequest
    ): Single<PaypalV2CreateTokenResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/create")
    fun createBillingAgreement(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body token: String
    ): Single<PaypalV2CreateAgreementResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/token/cancel")
    fun cancelToken(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body token: String
    ): Single<String?>

  }

  data class PaypalPayment(
    @SerializedName("callback_url") val callbackUrl: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("metadata") val metadata: String?,

    @SerializedName("origin") val origin: String?,
    @SerializedName("product") val sku: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("price.currency") val currency: String?,
    @SerializedName("price.value") val value: String?,
    @SerializedName("wallets.developer") val developer: String?,
    @SerializedName("entity.oemid") val entityOemId: String?,
    @SerializedName("entity.domain") val entityDomain: String?,
    @SerializedName("entity.promo_code") val entityPromoCode: String?,
    @SerializedName("wallets.user") val user: String?,
    @SerializedName("referrer_url") val referrerUrl: String?
  )

  data class CreateTokenRequest(
    @SerializedName("urls") val urls: Urls
  )

  data class Urls(
    @SerializedName("return") val returnUrl: String,
    @SerializedName("cancel") val cancelUrl: String
  )

}