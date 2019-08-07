package com.asfoundation.wallet.ui.gamification


interface MyLevelView {
  fun setupLayout()
  fun updateLevel(userStatus: UserRewardsStatus)
  fun showHowItWorksButton()
  fun setStaringLevel(userStatus: UserRewardsStatus)
  fun changeBottomSheetState()
  fun animateBackgroundFade()
}
