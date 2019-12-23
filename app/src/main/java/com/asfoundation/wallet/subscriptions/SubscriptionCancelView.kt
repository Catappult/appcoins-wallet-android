package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable

interface SubscriptionCancelView {

  fun navigateBack()
  fun showNoNetworkError()
  fun getBackClicks(): Observable<Any>
  fun getCancelClicks(): Observable<Any>
  fun showSubscriptionDetails(subscriptionDetails: ActiveSubscriptionDetails)
  fun showCancelSuccess()
  fun showLoading()
  fun showCancelError()
  fun showNoNetworkRetryAnimation()
  fun getRetryNetworkClicks(): Observable<Any>

}