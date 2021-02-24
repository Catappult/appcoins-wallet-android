package com.asfoundation.wallet.subscriptions.cancelsuccess

import io.reactivex.Observable

interface SubscriptionCancelSuccessView {

  fun getContinueClicks(): Observable<Any>
}