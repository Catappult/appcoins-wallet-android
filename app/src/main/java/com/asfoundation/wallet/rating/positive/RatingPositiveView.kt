package com.asfoundation.wallet.rating.positive

import io.reactivex.Observable

interface RatingPositiveView {
  fun rateAppClickEvent(): Observable<Any>

  fun remindMeLaterClickEvent(): Observable<Any>

  fun noClickEvent(): Observable<Any>

  fun initializeView(isNotFirstTime: Boolean)
}
