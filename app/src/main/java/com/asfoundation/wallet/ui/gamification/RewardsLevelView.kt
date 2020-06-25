package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface RewardsLevelView {

  fun getInfoButtonClick(): Observable<Any>

  fun showMainView()

  fun showRetryAnimation()

  fun showNetworkErrorView()

  fun retryClick(): Observable<Any>

  fun loadLegacyGamificationView()

  fun loadGamificationView()
}
