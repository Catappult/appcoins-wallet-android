package com.asfoundation.wallet.ui.gamification


interface MyLevelView {
  fun setupLayout()

  fun updateLevel(lastShownLevel: Int, level: Int,
                  bonus: List<Double>, pioneer: Boolean)

  fun setStaringLevel(lastShownLevel: Int, level: Int,
                      bonus: List<Double>, pioneer: Boolean)

  fun changeBottomSheetState()

  fun animateBackgroundFade()

  fun showPioneerUser()

  fun showNonPioneerUser()
}
