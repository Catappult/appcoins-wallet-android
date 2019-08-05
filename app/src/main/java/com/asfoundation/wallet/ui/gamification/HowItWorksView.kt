package com.asfoundation.wallet.ui.gamification

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


interface HowItWorksView {
  fun close()
  fun showLevels(levels: List<ViewLevel>, currentLevel: Int)
  fun showPeekInformation(bonusEarned: BigDecimal, totalSpend: FiatValue)
  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
