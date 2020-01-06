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
  fun showGenericError()
  fun showNoNetworkRetryAnimation()
  fun showGenericRetryAnimation()
  fun getRetryNetworkClicks(): Observable<Any>
  fun getRetryGenericClicks(): Observable<Any>
  fun subscriptionClicks(): Observable<String>
  fun showSubscriptionDetails(packageName: String)

}