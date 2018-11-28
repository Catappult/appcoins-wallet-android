package com.appcoins.wallet.gamification.repository

import io.reactivex.Single

interface GamificationRepository {
  fun getUserStatus(wallet: String): Single<UserStats>
  fun getLevels(): Single<Levels>
}
