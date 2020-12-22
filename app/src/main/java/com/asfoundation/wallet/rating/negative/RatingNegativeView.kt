package com.asfoundation.wallet.rating.negative

import io.reactivex.Observable

interface RatingNegativeView {
  fun submitClickEvent(): Observable<String>

  fun noClickEvent(): Observable<Any>

  fun showEmptySuggestionsError()

  fun setLoading()
}
