package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.ui.gamification.UserRewardsStatus

interface PromotionsView {
  fun setupLayout()
  fun setStaringLevel(userStatus: UserRewardsStatus)
  fun updateLevel(userStatus: UserRewardsStatus)
}
