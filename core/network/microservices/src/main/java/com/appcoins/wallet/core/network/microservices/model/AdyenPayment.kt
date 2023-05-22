package com.appcoins.wallet.core.network.microservices.model

import com.adyen.checkout.core.model.ModelObject
import com.google.gson.annotations.SerializedName


data class AdyenPayment(
  @SerializedName("payment.details") val details: Any,
  @SerializedName("payment.data") val data: String?
)

data class DisableWallet(@SerializedName("wallet.address") val walletAddress: String)


data class PaymentDetails(
  @SerializedName("payment.method") val adyenPaymentMethod: ModelObject,
  @SerializedName("payment.store_method") val shouldStoreMethod: Boolean,
  @SerializedName("payment.return_url") val returnUrl: String,
  @SerializedName("payment.shopper_interaction") val shopperInteraction: String?,
  @SerializedName("payment.billing_address")
  val billingAddress: AdyenBillingAddress?,
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("domain") val domain: String?,
  @SerializedName("metadata") val metadata: String?,
  @SerializedName("method") val method: String?,
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