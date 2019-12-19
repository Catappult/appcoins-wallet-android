package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal

data class SubscriptionItem(
    val appName: String,
    val packageName: String,
    val iconUrl: String,
    val amount: BigDecimal,
    val symbol: String,
    val recurrence: String
)