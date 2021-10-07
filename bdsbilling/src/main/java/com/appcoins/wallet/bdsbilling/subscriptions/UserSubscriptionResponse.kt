package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.subscriptions.ApplicationInfoResponse
import com.appcoins.wallet.bdsbilling.subscriptions.OrderResponse
import com.google.gson.annotations.SerializedName

/**
 * Object of a subscription to be shown on app for the user to manage
 * @param period Period of subscription, example "P1M"
 * @param title Name of the sku
 * @param subStatus Status of the subscription
 * @param started Date when subscription started, null when subStatus is PENDING
 * @param renewal Date when subscription is renewed, null when subStatus is PENDING, PAUSED, EXPIRED or REVOKED
 * @param expiry Date when subscription will expire or has expired, null when subStatus is PENDING or PAUSED
 * @param ended Date when subscription ended, null when subStatus is PENDING, ACTIVE, GRACE, HOLD, CANCELED or PAUSED
 *
 */
data class UserSubscriptionResponse(val uid: String, val sku: String, val title: String,
                                    val period: String, @SerializedName("substatus")
                                    val subStatus: SubscriptionSubStatus, val started: String?,
                                    val renewal: String?, val expiry: String?, val ended: String?,
                                    val application: ApplicationInfoResponse,
                                    val order: OrderResponse)

enum class SubscriptionSubStatus {
  PENDING, ACTIVE, PAUSED, GRACE, HOLD, CANCELED, EXPIRED, REVOKED
}
