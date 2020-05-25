package com.appcoins.wallet.bdsbilling

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class SellerDomain(val domain: String)

data class SubscriptionsResponse(val items: List<SubscriptionResponse>)

data class SubscriptionResponse(val sku: String,
                                val period: String,
                                @SerializedName("trial_period")
                                val trialPeriod: String?,
                                val title: String,
                                val description: String,
                                val price: Price,
                                val intro: Intro)

data class Price(val currency: String, val value: BigDecimal, val label: String, val micros: Long,
                 val appc: AppcPrice)

data class AppcPrice(val value: BigDecimal, val micros: Long)

data class Intro(val period: String, val cycles: String, val price: Price)

data class SubscriptionPurchasListResponse(val items: List<SubscriptionPurchaseResponse>)

/**
 * @param acknowledged Whether or not the subscription purchase was acknowledged by the in-app seller.
 * @param consumed Whether or not the subscription purchase was consumed by the buyer.
 * @param created The subscription purchase creation timestamp, in UTC using the ISO 8601 format, including milliseconds.
Example: 2020-03-01T10:27:45.823Z
 * @param modified The subscription purchase last modification timestamp, in UTC using the ISO 8601 format, including milliseconds.
Example: 2020-03-01T10:27:45.823Z
 * @param verification The subscription purchase verification object, to be used by the in-app seller to cryptographically verify the subscription purchase, in order to acknowledge and activate it.
 */
data class SubscriptionPurchaseResponse(val uid: String,
                                        val sku: String,
                                        val status: Status,
                                        @SerializedName("order_reference")
                                        val orderReference: String,
                                        @SerializedName("auto_renewing")
                                        val autoRenewing: Boolean,
                                        val acknowledged: Boolean,
                                        val consumed: Boolean,
                                        val payload: String?,
                                        val created: String,
                                        val modified: String,
                                        val verification: Verification)

/**
 * @param type Always GOOGLE as of right now
 * @param data The subscription purchase verification data. Example:
If verification.type is GOOGLE, then the expected data format is a JSON object in the following format:
{
"orderId": [order_reference],
"packageName": (string),
"productId": [sku],
"purchaseTime": (integer),
"purchaseToken": [uid],
"purchaseState": (integer),
"developerPayload": [payload]
}
 * @param signature Base64-encoded string, generated from verification.data.
 * If verification.type is GOOGLE, then the signature is generated using the RSASSA-PKCS1-v1_5 scheme.
 */
data class Verification(val type: String, val data: String, val signature: String)

/**
 * @param status The status property is only allowed to be updated as follows:
from COMPLETED to ACTIVE : to activate a successfully completed subscription purchase;
from ACTIVE to PAUSED : to pause an active subscription purchase;
from ACTIVE to CANCELED : to cancel an active subscription purchase;
from PAUSED to ACTIVE : to resume a paused subscription purchase.
 * @param acknowledged
 * @param consumed Theses properties are only allowed to be updated from false to true.
 */
data class PurchaseUpdate(val status: Status, val autoRenewing: Boolean, val acknowledged: Boolean,
                          val consumed: Boolean)

enum class Status {
  PENDING, //The subscription purchase is pending acknowledgement from the in-app seller.
  COMPLETED, //The subscription purchase is successfully completed, and may now be activated.
  ACTIVE,//The subscription purchase is currently active.
  PAUSED,//The subscription purchase is currently paused and will resume at a later time set by the buyer.
  EXPIRED,//The subscription purchase has expired.
  CANCELED,//The subscription purchase has been explicitly canceled.
  REVOKED//The subscription purchase has been explicitly revoked and a refund was issued to the buyer.
}

fun SubscriptionsResponse.merge(other: SubscriptionsResponse): SubscriptionsResponse {
  (items as ArrayList).addAll(other.items)
  return this
}