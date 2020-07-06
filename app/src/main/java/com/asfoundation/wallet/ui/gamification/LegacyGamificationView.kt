package com.asfoundation.wallet.ui.gamification


interface LegacyGamificationView {
  fun setLevelIcons()

  fun updateLevel(lastShownLevel: Int, level: Int, bonus: List<Double>)

  fun setStaringLevel(lastShownLevel: Int, level: Int, bonus: List<Double>)

  fun changeBottomSheetState()

  fun animateBackgroundFade()

  fun showPioneerUser()
}
