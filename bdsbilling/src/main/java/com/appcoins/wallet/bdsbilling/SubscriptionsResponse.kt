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
                                val intro: Intro? // The subscription introductory object. If null, then no introductory period has been set in the subscription.
)

data class Price(val currency: String, val value: BigDecimal, val label: String, val micros: Long,
                 val appc: AppcPrice)

data class AppcPrice(val value: BigDecimal, val label: String, val micros: Long)

data class Intro(val period: String, val cycles: String, val price: Price)

data class SubscriptionPurchasListResponse(val items: List<SubscriptionPurchaseResponse>)

/**
 * @param verification The subscription purchase verification object, to be used by the in-app seller to cryptographically verify the subscription purchase, in order to acknowledge and activate it.
 */
data class SubscriptionPurchaseResponse(val uid: String,
                                        val sku: String,
                                        val status: Status,
                                        val state: PurchaseState,
                                        @SerializedName("order_reference")
                                        val orderReference: String,
                                        @SerializedName("auto_renewing")
                                        val autoRenewing: Boolean,
                                        val payload: String?,
                                        val created: String,
                                        val verification: Verification)

data class Verification(val type: String, val data: String, val signature: String)

/**
 * @param state The state property is only allowed to be updated as follows:
 * from PENDING to ACKNOWLEDGED
 * to CONSUMED
 */
data class PurchaseUpdate(val state: PurchaseState)

enum class Status {
  PENDING, //The subscription purchase is pending acknowledgement from the in-app seller.
  COMPLETED, //The subscription purchase is successfully completed, and may now be activated.
  CANCELED,//The subscription purchase has been explicitly canceled.
}

enum class PurchaseState {
  PENDING, //The subscription purchase is pending acknowledgement.
  ACKNOWLEDGED, //The subscription purchase has been acknowledged.
  CONSUMED //The subscription purchase has been consumed.
}

fun SubscriptionsResponse.merge(other: SubscriptionsResponse): SubscriptionsResponse {
  (items as ArrayList).addAll(other.items)
  return this
}