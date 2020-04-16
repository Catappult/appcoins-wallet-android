package com.asfoundation.wallet.ui.gamification

import java.util.*


interface HowItWorksView {
  fun showLevels(levels: List<ViewLevel>, currentLevel: Int, updateDate: Date?)

  fun showPeekInformation(totalSpend: String, bonusEarned: String, currencySymbol: String)

  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
