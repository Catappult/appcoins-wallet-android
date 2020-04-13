package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.ui.iab.FiatValue
import java.util.*


interface HowItWorksView {
  fun showLevels(levels: List<ViewLevel>,
                 currentLevel: Int, updateDate: Date?)

  fun showPeekInformation(userStats: UserStats, bonusEarnedFiat: FiatValue)

  fun showNextLevelFooter(userStatus: UserRewardsStatus)
}
