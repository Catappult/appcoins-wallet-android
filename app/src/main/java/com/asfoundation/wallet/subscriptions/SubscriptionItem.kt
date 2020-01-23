package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal
import java.util.*

data class SubscriptionItem(
    val appName: String,
    val packageName: String,
    val iconUrl: String,
    val amount: BigDecimal,
    val symbol: String,
    val periodicity: String,
    val expiresOn: Date?
)