package com.asfoundation.wallet.subscriptions.cancel

import com.asfoundation.wallet.subscriptions.SubscriptionItem
import io.reactivex.Observable

interface SubscriptionCancelView {

  fun showNoNetworkError()

  fun getBackClicks(): Observable<Any>

  fun getCancelClicks(): Observable<Any>

  fun showSubscriptionDetails(subscriptionItem: SubscriptionItem)

  fun showLoading()

  fun showCancelError()

  fun showNoNetworkRetryAnimation()

  fun getRetryNetworkClicks(): Observable<Any>

  fun setTransitionName(transitionName: String)
}