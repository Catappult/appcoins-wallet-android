package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal

data class SubscriptionsResponse(val items: List<SubscriptionResponse>)

data class SubscriptionResponse(val sku: String, val period: String, val trialPeriod: String?,
                                val title: String, val description: String,
                                val price: Price, val intro: Intro)

data class Price(val currency: String, val value: BigDecimal, val label: String, val micros: Long,
                 val appc: AppcPrice)

data class AppcPrice(val value: BigDecimal, val micros: Long)

data class Intro(val period: String, val cycles: String, val price: Price)

data class SubscriptionsPurchaseResponse(val items: List<SubscriptionPurchase>)

data class SubscriptionPurchase(val uid: String, val sku: String, val status: Status,
                                val orderReference: String, val autoRenewing: Boolean,
                                val payload: String?, val created: String, val modified: String,
                                val verification: Verification)

data class Verification(val type: String, val data: String, val signature: String)

enum class Status {
  PENDING, ACKNOWLEDGED, ACTIVE, PAUSED, EXPIRED, CANCELED, REVOKED
}