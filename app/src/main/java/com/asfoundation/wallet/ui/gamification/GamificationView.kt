package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.LevelViewModel
import io.reactivex.Observable
import java.math.BigDecimal

interface GamificationView {

  fun displayGamificationInfo(currentLevel: Int, nextLevelAmount: BigDecimal,
                              levels: List<LevelViewModel>,
                              totalSpend: BigDecimal)

  fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String)

  fun getLevelsClicks(): Observable<Boolean>

  fun toogleReachedLevels(it: Boolean)
}
