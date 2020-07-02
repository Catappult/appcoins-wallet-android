package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.LevelViewModel
import io.reactivex.Observable
import java.math.BigDecimal
import java.util.*

interface GamificationView {

  fun displayGamificationInfo(currentLevel: Int, nextLevelAmount: BigDecimal,
                              levels: List<LevelViewModel>,
                              totalSpend: BigDecimal,
                              updateDate: Date?)

  fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String)

  fun getLevelsClicks(): Observable<Boolean>

  fun toogleReachedLevels(hide: Boolean)
}
