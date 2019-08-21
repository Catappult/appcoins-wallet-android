package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.ui.iab.FiatValue


interface HowItWorksView {
  fun showLevels(levels: List<ViewLevel>, currentLevel: Int)
  fun showPeekInformation(userStats: UserStats, bonusEarnedFiat: FiatValue)
  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
