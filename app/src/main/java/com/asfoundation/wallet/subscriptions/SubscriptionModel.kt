package com.asfoundation.wallet.subscriptions

import java.io.Serializable

data class SubscriptionModel(val activeSubscriptions: List<SubscriptionItem>,
                             val expiredSubscriptions: List<SubscriptionItem>,
                             val isEmpty: Boolean = false,
                             val fromCache: Boolean = false,
                             val error: Error? = null) : Serializable {

  constructor(showEmpty: Boolean, fromCache: Boolean, error: Error?) : this(emptyList(),
      emptyList(), showEmpty, fromCache, error)

  constructor(fromCache: Boolean, error: Error?) : this(emptyList(),
      emptyList(), true, fromCache, error)
}