package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import java.net.UnknownHostException

class BdsGamificationRepository(private val api: GamificationApi) :
    GamificationRepository {
  override fun getUserStatus(wallet: String): Single<UserStatus> {
    return api.getUserStatus(wallet).map { map(it) }.onErrorReturn { map(it) }
  }

  private fun map(throwable: Throwable): UserStatus {
    return when (throwable) {
      is UnknownHostException -> UserStatus(UserStatus.Status.NO_NETWORK)
      else -> {
        throw throwable
      }
    }
  }

  private fun map(response: UserStatusResponse): UserStatus {
    return UserStatus(UserStatus.Status.OK, response.level,
        response.nextLevelAmount, response.bonus, response.totalSpend)
  }

  override fun getLevels(): Single<Levels> {
    return api.getLevels().map { map(it) }.onErrorReturn { Levels(Levels.Status.NO_NETWORK) }
  }

  private fun map(response: LevelsResponse): Levels {
    val list = mutableListOf<Levels.Level>()
    for (level in response.list) {
      list.add(Levels.Level(level.amount, level.bonus, level.level))
    }
    return Levels(Levels.Status.OK, list.toList())
  }
}