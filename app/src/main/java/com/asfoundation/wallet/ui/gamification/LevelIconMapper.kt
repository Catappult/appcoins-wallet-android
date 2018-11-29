package com.asfoundation.wallet.ui.gamification

import com.asf.wallet.R

class LevelIconMapper {
  fun map(level: ViewLevel): Int {
    if (level.isCompleted) {
      return when (level.level) {
        0 -> R.drawable.ic_appc
        1 -> R.drawable.ic_appc
        2 -> R.drawable.ic_appc
        3 -> R.drawable.ic_appc
        4 -> R.drawable.ic_appc
        5 -> R.drawable.ic_appc
        else -> {
          R.drawable.ic_level_locked
        }
      }
    }
    return R.drawable.ic_level_locked
  }

}
