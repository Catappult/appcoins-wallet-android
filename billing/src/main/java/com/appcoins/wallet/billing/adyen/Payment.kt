package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.core.model.ModelObject
import com.google.gson.annotations.SerializedName

data class Payment(@SerializedName("payment.method") val adyenPaymentMethod: ModelObject,
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
                   @SerializedName("wallets.store") val store: String?,
                   @SerializedName("wallets.oem") val oem: String?,
                   @SerializedName("wallets.user") val user: String?,
                   @SerializedName("referrerUrl") val referrerUrl: String?)