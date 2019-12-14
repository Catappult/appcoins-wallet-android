package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal
import java.util.*

data class SubscriptionDetails(
    val appName: String,
    val packageName: String,
    val iconUrl: String,
    val amount: BigDecimal,
    val symbol: String,
    val currency: String,
    val status: SubscriptionStatus,
    var appcValue: BigDecimal,
    val paymentMethod: String,
    val paymentMethodUrl: String,
    val nextPayment: Date? = null,
    val lastBill: Date? = null,
    val startDate: Date? = null
)