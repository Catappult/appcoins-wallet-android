package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable


interface SubscriptionDetailsView {

  fun getBackClicks(): Observable<Any>
  fun getCancelClicks(): Observable<String>
  fun navigateBack()
  fun showActiveDetails(subscriptionDetails: SubscriptionDetails)
  fun showExpiredDetails(subscriptionDetails: SubscriptionDetails)
  fun showNoNetworkError()
  fun cancelSubscription()
  fun showLoading()
  fun showDetails()

}