package com.asfoundation.wallet.subscriptions.success

import io.reactivex.Observable

interface SubscriptionSuccessView {

  fun getContinueClicks(): Observable<Any>

  fun setupUi(successType: SubscriptionSuccessFragment.SubscriptionSuccess)
}