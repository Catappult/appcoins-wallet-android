package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable

interface EarnAppcoinsView {

  fun backButtonClick(): Observable<Any>
  fun discoverButtonClick(): Observable<Any>
  fun navigateBack()
  fun backPressed(): Observable<Any>
}
