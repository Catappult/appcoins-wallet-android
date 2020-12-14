package com.asfoundation.wallet.promotions

import io.reactivex.Observable

interface PromotionsActivityView {

  fun backPressed(): Observable<Any>

  fun enableBack()

  fun disableBack()
}
