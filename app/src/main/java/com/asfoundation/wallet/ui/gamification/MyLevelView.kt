package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface MyLevelView {
  fun setupLayout()

  fun updateLevel(userStatus: UserRewardsStatus, hasNewLevel: Boolean)

  fun getButtonClicks(): Observable<Any>

  fun showHowItWorksScreen()

  fun showHowItWorksButton()

}
