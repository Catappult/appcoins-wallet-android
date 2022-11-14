package com.asfoundation.wallet.billing

import com.asfoundation.wallet.billing.paypal.PaypalTransaction
import com.asfoundation.wallet.billing.paypal.PaypalV2CreateAgreementResponse
import com.asfoundation.wallet.billing.paypal.PaypalV2CreateTokenResponse
import com.asfoundation.wallet.billing.paypal.PaypalV2StartResponse
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeBonusResponse
import com.asfoundation.wallet.promo_code.repository.ValidityState
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject

class PayPalV2Repository @Inject constructor(
  private val paypalV2Api: PaypalV2Api
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
          PaypalTransaction.PaypalValidityState.COMPLETED
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
  ): Single<PaypalV2CreateTokenResponse> {
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
        response //TODO map
        //TODO error
      }
  }

  fun createBillingAgreement(
    walletAddress: String,
    walletSignature: String,
    token: String
  ): Single<PaypalV2CreateAgreementResponse> {
    return paypalV2Api.createBillingAgreement(
      walletAddress,
      walletSignature,
      token = token
      )
      .map { response: PaypalV2CreateAgreementResponse ->
        response //TODO map
        //TODO error
      }
  }

  private fun handleCreateTransactionErrorCodes(errorCode: Int?): PaypalTransaction {
    val validity = when (errorCode) {
      404 -> PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT
//    ... ->
      else -> PaypalTransaction.PaypalValidityState.ERROR
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