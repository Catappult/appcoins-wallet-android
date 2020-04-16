package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.ui.iab.FiatValue


interface HowItWorksView {
  fun showLevels(levels: List<ViewLevel>, currentLevel: Int)
  fun showPeekInformation(totalSpend: String, bonusEarned: String, currencySymbol: String)
  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
