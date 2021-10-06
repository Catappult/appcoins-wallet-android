package com.asfoundation.wallet.subscriptions.cancel

import com.asfoundation.wallet.subscriptions.SubscriptionItem

data class SubscriptionCancelData(val subscriptionItem: SubscriptionItem,
                                  val transitionName: String)
