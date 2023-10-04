package com.asfoundation.wallet.subscriptions.details

import android.view.View
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import io.reactivex.Observable

interface SubscriptionDetailsView {

  fun getCancelClicks(): Observable<Any>

  fun getRenewSubscriptionClicks(): Observable<Any>

  fun getRetryClicks(): Observable<Any>

  fun setActiveDetails(subscriptionItem: SubscriptionItem)

  fun setExpiredDetails(subscriptionItem: SubscriptionItem)

  fun retrieveSharedElement(): View

  fun setupTransitionName(transitionName: String)

  fun showLoading()

  fun hideLoading()

  fun showNoNetworkError()

  fun showRenewError()

  fun showDetails()
}