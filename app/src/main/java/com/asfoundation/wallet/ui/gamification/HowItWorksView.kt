package com.asfoundation.wallet.ui.gamification

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


interface HowItWorksView {
  fun showLevels(levels: List<ViewLevel>, currentLevel: Int)
  fun showPeekInformation(totalSpend: BigDecimal, bonusEarned: FiatValue)
  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
