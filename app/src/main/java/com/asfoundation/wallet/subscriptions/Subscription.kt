package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal
import java.util.*

data class Subscription(
    val appName: String,
    val packageName: String,
    val iconUrl: String,
    val amount: BigDecimal,
    val symbol: String,
    val currency: String,
    val active: Boolean,
    val paymentMethod: String,
    val paymentMethodIcon: String,
    val nextPaymentDate: Date?,
    val lastBill: Date?,
    val startDate: Date,
    val recurrence: String
)