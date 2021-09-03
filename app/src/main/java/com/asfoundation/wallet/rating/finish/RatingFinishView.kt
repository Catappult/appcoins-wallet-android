package com.asfoundation.wallet.rating.finish

import io.reactivex.Observable

interface RatingFinishView {
  fun animationEndEvent(): Observable<Any>
}