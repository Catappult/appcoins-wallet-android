package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class SubscriptionsResponse(val items: List<SubscriptionResponse>)

data class SubscriptionResponse(val sku: String,
                                val period: String,
                                @SerializedName("trial_period")
                                val trialPeriod: String?,
                                val title: String,
                                val description: String,
                                val subscriptionPrice: SubscriptionPrice
)

data class SubscriptionPrice(
  val currency: String, val value: BigDecimal, val label: String, val symbol: String,
  val micros: Long, val appc: SubscriptionAppcPrice
)

data class SubscriptionAppcPrice(val value: BigDecimal, val label: String, val micros: Long)

data class SubscriptionPurchaseListResponse(val items: List<SubscriptionPurchaseResponse>)

/**
 * @param created The creation timestamp, in UTC using the ISO 8601 format, including microseconds.
 * @param renewal The renewal timestamp, in UTC using the ISO 8601 format, including microseconds.
If null, then no renewal has been set yet (when state is PENDING).
 * @param verification The subscription purchase verification object, to be used by the in-app seller to cryptographically verify the subscription purchase, in order to acknowledge and activate it.
 */
data class SubscriptionPurchaseResponse(val uid: String,
                                        val sku: String,
                                        val state: PurchaseState,
                                        @SerializedName("order_uid")
                                        val orderUid: String,
                                        @SerializedName("auto_renewing")
                                        val autoRenewing: Boolean,
                                        val payload: String?,
                                        val created: String,
                                        val renewal: String?,
                                        val verification: Verification)

fun SubscriptionsResponse.merge(other: SubscriptionsResponse): SubscriptionsResponse {
  (items as ArrayList).addAll(other.items)
  return this
}