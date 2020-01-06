package com.asfoundation.wallet.subscriptions

data class SubscriptionModel(
    val activeSubscriptions: List<SubscriptionItem>,
    val expiredSubscriptions: List<SubscriptionItem>,
    val isEmpty: Boolean = false
)