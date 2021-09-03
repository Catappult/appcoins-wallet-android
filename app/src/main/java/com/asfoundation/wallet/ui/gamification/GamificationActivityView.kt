package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface GamificationActivityView {

  fun showMainView()

  fun showRetryAnimation()

  fun showNetworkErrorView()

  fun retryClick(): Observable<Any>

  fun loadGamificationView()

  fun backPressed(): Observable<Any>

  fun enableBack()

  fun disableBack()
}
