package com.appcoins.wallet.bdsbilling

import java.math.BigDecimal

data class SellerDomain(val domain: String)

data class SubscriptionsResponse(val items: List<SubscriptionResponse>)

data class SubscriptionResponse(val sku: String, val period: String, val trialPeriod: String?,
                                val title: String, val description: String,
                                val price: Price, val intro: Intro)

data class Price(val currency: String, val value: BigDecimal, val label: String, val micros: Long,
                 val appc: AppcPrice)

data class AppcPrice(val value: BigDecimal, val micros: Long)

data class Intro(val period: String, val cycles: String, val price: Price)

data class PurchaseResponse(val items: List<Purchase>)

data class Purchase(val uid: String, val sku: String, val status: Status,
                    val orderReference: String, val autoRenewing: Boolean,
                    val payload: String?, val created: String, val modified: String,
                    val verification: Verification)

data class Verification(val type: String, val data: String, val signature: String)

data class PurchaseUpdate(val status: Status, val autoRenewing: Boolean)

enum class Status {
  PENDING, //The subscription purchase is pending acknowledgement from the in-app seller.
  ACKNOWLEDGED, //The subscription purchase has been acknowledged by the in-app seller, and may now be activated.
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