package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable
import java.util.*

interface GamificationView {

  fun displayGamificationInfo(updateDate: Date?)

  fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String)

  fun getUiClick(): Observable<Pair<String, Boolean>>

  fun toggleReachedLevels(show: Boolean)

  fun getHomeBackPressed(): Observable<Any>

  fun handleBackPressed()

  fun getBottomSheetButtonClick(): Observable<Any>

  fun getBackPressed(): Observable<Any>

  fun updateBottomSheetVisibility()

  fun getBottomSheetContainerClick(): Observable<Any>
}
