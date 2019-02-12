package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.GamificationLocalData
import io.reactivex.Completable
import io.reactivex.Single

class GamificationLocalDataTest : GamificationLocalData {
  var lastShownLevelResponse: Single<Int>? = null
  private var wallet: String? = null

  override fun saveShownLevel(wallet: String, level: Int): Completable {
    return Completable.fromAction {
      this.wallet = wallet
      lastShownLevelResponse = Single.just(level)
    }
  }

  fun getWallet(): String {
    val aux = wallet!!
    wallet = null
    return aux
  }

  override fun getLastShownLevel(wallet: String): Single<Int> {
    val aux = lastShownLevelResponse!!
    lastShownLevelResponse = null
    return aux
  }
}