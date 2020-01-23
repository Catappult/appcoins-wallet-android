package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable


interface SubscriptionDetailsView {

  fun getBackClicks(): Observable<Any>
  fun getCancelClicks(): Observable<Any>
  fun navigateBack()
  fun setActiveDetails(subscriptionDetails: ActiveSubscriptionDetails)
  fun setExpiredDetails(subscriptionDetails: ExpiredSubscriptionDetails)
  fun showNoNetworkError()
  fun cancelSubscription()
  fun showLoading()
  fun showDetails()
  fun showGenericError()
  fun showNoNetworkRetryAnimation()
  fun showGenericRetryAnimation()
  fun getRetryNetworkClicks(): Observable<Any>
  fun getRetryGenericClicks(): Observable<Any>

}