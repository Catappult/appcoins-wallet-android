package com.asfoundation.wallet.rating.entry

import io.reactivex.Observable

interface RatingEntryView {
  fun yesClickEvent(): Observable<Any>

  fun noClickEvent(): Observable<Any>
}
