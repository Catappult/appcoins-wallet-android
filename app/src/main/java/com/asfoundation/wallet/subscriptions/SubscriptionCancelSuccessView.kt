package com.asfoundation.wallet.subscriptions

import io.reactivex.Observable

interface SubscriptionCancelSuccessView {

  fun navigateBack()
  fun getContinueClicks(): Observable<Any>

}