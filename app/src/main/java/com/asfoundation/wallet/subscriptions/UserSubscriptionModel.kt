package com.asfoundation.wallet.subscriptions

data class UserSubscriptionListModel(val userSubscriptionItems: List<SubscriptionItem>,
                                     val error: Error? = null) {
  constructor(error: Error) : this(emptyList(), error)
}

enum class Error {
  UNKNOWN, NO_NETWORK
}