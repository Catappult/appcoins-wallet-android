package com.asfoundation.wallet.subscriptions

import java.math.BigDecimal
import java.util.*

open class SubscriptionDetails(
    open val appName: String,
    open val packageName: String,
    open val iconUrl: String,
    open val amount: BigDecimal,
    open val symbol: String,
    open val currency: String,
    open val recurrence: String,
    open var appcValue: BigDecimal,
    open val paymentMethod: String,
    open val paymentMethodUrl: String
)

data class ActiveSubscriptionDetails(
    override val appName: String,
    override val packageName: String,
    override val iconUrl: String,
    override val amount: BigDecimal,
    override val symbol: String,
    override val currency: String,
    override val recurrence: String,
    override var appcValue: BigDecimal,
    override val paymentMethod: String,
    override val paymentMethodUrl: String,
    val nextPayment: Date
) : SubscriptionDetails(appName, packageName, iconUrl, amount, symbol, currency, recurrence,
    appcValue, paymentMethod, paymentMethodUrl)


data class ExpiredSubscriptionDetails(
    override val appName: String,
    override val packageName: String,
    override val iconUrl: String,
    override val amount: BigDecimal,
    override val symbol: String,
    override val currency: String,
    override val recurrence: String,
    override var appcValue: BigDecimal,
    override val paymentMethod: String,
    override val paymentMethodUrl: String,
    val lastBill: Date,
    val startDate: Date
) : SubscriptionDetails(appName, packageName, iconUrl, amount, symbol, currency, recurrence,
    appcValue, paymentMethod, paymentMethodUrl)

class EmptySubscriptionDetails :
    SubscriptionDetails("", "", "", BigDecimal.ZERO, "",
        "", "", BigDecimal.ZERO, "", "")