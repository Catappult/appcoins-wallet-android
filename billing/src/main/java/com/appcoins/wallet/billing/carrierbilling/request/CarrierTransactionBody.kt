package com.appcoins.wallet.billing.carrierbilling.request

import com.google.gson.annotations.SerializedName

data class CarrierTransactionBody(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("return_url") val returnUrl: String,
    @SerializedName("method") val method: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("origin") val origin: String?,
    @SerializedName("product") val sku: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("price.currency") val currency: String?,
    @SerializedName("price.value") val value: String?,
    @SerializedName("wallets.developer") val developer: String?,
    @SerializedName("wallets.oem") val oem: String?,
    @SerializedName("wallets.store") val store: String?,
    @SerializedName("wallets.user") val user: String?
)