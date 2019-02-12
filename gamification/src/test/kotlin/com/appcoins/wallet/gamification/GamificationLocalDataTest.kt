package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.GamificationLocalData
import io.reactivex.Single

class GamificationLocalDataTest : GamificationLocalData {
  var lastShownLevelResponse: Single<Int>? = null
  override fun getLastShownLevel(wallet: String): Single<Int> {
    val aux = lastShownLevelResponse!!
    lastShownLevelResponse = null
    return aux
  }
}