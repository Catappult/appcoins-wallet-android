package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Observable

interface PromotionsView {
  fun setupLayout()
  fun setStaringLevel(userStatus: UserRewardsStatus)
  fun updateLevel(userStatus: UserRewardsStatus)
  fun seeMoreClick(): Observable<Any>
  fun navigateToGamification()
}
