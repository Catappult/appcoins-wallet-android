package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.util.Period
import java.io.Serializable
import java.math.BigDecimal
import java.util.*

data class SubscriptionItem(val sku: String, val itemName: String, val period: Period?,
                            val status: Status,
                            val started: Date?, val renewal: Date?, val expiry: Date?,
                            val ended: Date?, val packageName: String, val appName: String,
                            val appIcon: String, val fiatAmount: BigDecimal, val fiatSymbol: String,
                            val currency: String, val paymentMethod: String,
                            val paymentIcon: String, val appcAmount: BigDecimal,
                            val appcLabel: String, val uid: String) : Serializable {

  fun isActiveSubscription(): Boolean {
    return status == Status.ACTIVE || status == Status.CANCELED || status == Status.PAUSED ||
        status == Status.GRACE
  }
}

enum class Status {
  ACTIVE, CANCELED, EXPIRED, PAUSED, PENDING, REVOKED, GRACE, HOLD
}
