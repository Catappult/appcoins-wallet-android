package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.Levels
import java.math.BigDecimal

interface GamificationView {

  fun displayGamificationInfo(currentLevel: Int, levels: List<Levels.Level>, totalSpend: BigDecimal)

  fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String)
}
