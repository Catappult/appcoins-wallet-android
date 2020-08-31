package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface GamificationActivityView {

  fun getInfoButtonClick(): Observable<Any>

  fun showMainView()

  fun showRetryAnimation()

  fun showNetworkErrorView()

  fun retryClick(): Observable<Any>

  fun loadGamificationView()
}
