package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable

interface SubscriptionListView {

  fun onActiveSubscriptions(subscriptions: List<SubscriptionItem>)
  fun onExpiredSubscriptions(subscriptions: List<SubscriptionItem>)
  fun showSubscriptions()
  fun showNoSubscriptions()
  fun showNoNetworkError()
  fun showLoading()
  fun retryClick(): Observable<Any>
  fun showRetryAnimation()

}