package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface MyLevelView {
  fun updateLevel(userStatus: UserRewardsStatus)

  fun getButtonClicks(): Observable<Any>

  fun showHowItWorksScreen()
}
