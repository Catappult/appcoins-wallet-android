package com.asfoundation.wallet.subscriptions

import java.io.Serializable

data class SubscriptionModel(val allSubscriptions: List<SubscriptionItem>,
                             val expiredSubscriptions: List<SubscriptionItem>,
                             val fromCache: Boolean = false,
                             val error: Error? = null) : Serializable {

  constructor(fromCache: Boolean, error: Error?) : this(emptyList(), emptyList(), fromCache, error)

  enum class Error {
    UNKNOWN, NO_NETWORK
  }
}