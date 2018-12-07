package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.GamificationRepository
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import io.reactivex.Single

class Gamification(private val repository: GamificationRepository) {
  fun getUserStatus(wallet: String): Single<UserStats> {
    return repository.getUserStatus(wallet)
  }

  fun getLevels(): Single<Levels> {
    return repository.getLevels()
  }
}
