package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Observable

interface PromotionsView {
  fun setupLayout()
  fun setStaringLevel(userStatus: UserRewardsStatus)
  fun updateLevel(userStatus: UserRewardsStatus)
  fun seeMoreClick(): Observable<Any>
  fun navigateToGamification()
  fun detailsClick(): Observable<Any>
  fun navigateToPromotionDetails()
  fun shareClick(): Observable<Any>
  fun showShare()
  fun gamificationCardClick(): Observable<Any>
  fun referralCardClick(): Observable<Any>
  fun showGamificationUpdate(show: Boolean)
  fun showReferralUpdate(show: Boolean)
  fun showReferralCard()
  fun showGamificationCard()
  fun showNetworkErrorView()
  fun retryClick(): Observable<Any>
  fun showRetryAnimation()
}
