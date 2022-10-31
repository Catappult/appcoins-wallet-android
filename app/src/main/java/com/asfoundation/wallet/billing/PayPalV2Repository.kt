package com.asfoundation.wallet.billing

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.billing.paypal.PaypalV2StartResponse
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
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
  ): Single<PaypalV2StartResponse> {  //TODO
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
        response //TODO
      }
  }

  interface PaypalV2Api {
    @POST("8.20200815/gateways/paypal/transactions")
    fun createTransaction(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body paypalPayment: PaypalPayment
    ): Single<PaypalV2StartResponse>

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

}