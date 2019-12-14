package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable

interface SubscriptionCancelView {

  fun navigateBack()
  fun showNoNetworkError()
  fun getBackClicks(): Observable<Any>
  fun getCancelClicks(): Observable<String>
  fun showSubscriptionDetails(subscriptionDetails: SubscriptionDetails)
  fun showCancelSuccess()
  fun showLoading()
  fun showCancelError()

}