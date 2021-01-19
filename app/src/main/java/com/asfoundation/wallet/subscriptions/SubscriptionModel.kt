package com.asfoundation.wallet.subscriptions

data class SubscriptionModel(val activeSubscriptions: List<SubscriptionItem>,
                             val expiredSubscriptions: List<SubscriptionItem>,
                             val isEmpty: Boolean = false,
                             val error: Error? = null) {

  constructor(isEmpty: Boolean, error: Error?) : this(emptyList(), emptyList(), isEmpty, error)
}