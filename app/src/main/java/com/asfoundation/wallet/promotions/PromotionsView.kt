package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.ui.gamification.CurrentLevelInfo
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Observable

interface PromotionsView {
  fun setLevelIcons()
  fun setStaringLevel(userStatus: UserRewardsStatus)
  fun setLevelInformation(userStatus: UserRewardsStatus,
                          legacy: Boolean)

  fun seeMoreClick(): Observable<Any>
  fun legacySeeMoreClick(): Observable<Any>
  fun detailsClick(): Observable<Any>
  fun shareClick(): Observable<Any>
  fun legacyGamificationCardClick(): Observable<Any>
  fun gamificationCardClick(): Observable<Any>
  fun referralCardClick(): Observable<Any>
  fun showGamificationUpdate(show: Boolean)
  fun showLegacyGamificationUpdate(show: Boolean)
  fun showReferralUpdate(show: Boolean)
  fun showReferralCard()
  fun showLegacyGamificationCard()
  fun showGamificationCard(currentLevelInfo: CurrentLevelInfo, bonus: Double)
  fun showNetworkErrorView()
  fun retryClick(): Observable<Any>
  fun showRetryAnimation()
  fun setReferralBonus(bonus: String, currency: String)
  fun toggleShareAvailability(validated: Boolean)
  fun hideLoading()
  fun showLoading()
  fun showNoPromotionsScreen()
}
