package com.asfoundation.wallet.subscriptions

interface SubscriptionView {

  fun showSubscriptionList()
  fun showSubscriptionDetails(packageName: String?)
  fun showCancelSubscription(packageName: String)
  fun showCancelSuccess()
  fun navigateBack()
  fun endCancelSubscription()
}
