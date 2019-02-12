package com.appcoins.wallet.gamification.repository

import io.reactivex.Single

interface GamificationLocalData {
  /**
   * @return -1 if never showed any level
   */
  fun getLastShownLevel(wallet: String): Single<Int>
}
