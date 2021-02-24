package com.asfoundation.wallet.subscriptions.list

import android.view.View
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import io.reactivex.Observable

interface SubscriptionListView {

  fun onActiveSubscriptions(subscriptionModels: List<SubscriptionItem>)

  fun onExpiredSubscriptions(subscriptionModels: List<SubscriptionItem>)

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

  fun subscriptionClicks(): Observable<Pair<SubscriptionItem, View>>

  fun hasItems(): Boolean
}