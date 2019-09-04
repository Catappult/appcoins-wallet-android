package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException

class BdsPromotionsRepository(private val api: GamificationApi,
                              private val local: GamificationLocalData,
                              private val versionCode: String) :
    PromotionsRepository {

  override fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
    return local.getLastShownLevel(wallet, screen)
  }

  override fun shownLevel(wallet: String, level: Int, screen: String): Completable {
    return local.saveShownLevel(wallet, level, screen)
  }

  override fun getForecastBonus(wallet: String, packageName: String,
                                amount: BigDecimal): Single<ForecastBonus> {
    return api.getForecastBonus(wallet, packageName, amount, "APPC")
        .map { map(it) }
        .onErrorReturn { mapForecastError(it) }
  }

  private fun mapForecastError(throwable: Throwable): ForecastBonus {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> ForecastBonus(ForecastBonus.Status.NO_NETWORK)
      else -> {
        ForecastBonus(ForecastBonus.Status.UNKNOWN_ERROR)
      }
    }
  }

  private fun map(bonusResponse: ForecastBonusResponse): ForecastBonus {
    if (bonusResponse.status == ForecastBonusResponse.Status.ACTIVE) {
      return ForecastBonus(ForecastBonus.Status.ACTIVE, bonusResponse.bonus)
    }
    return ForecastBonus(ForecastBonus.Status.INACTIVE)
  }

  override fun getUserStats(wallet: String): Single<UserStats> {
    return api.getUserStatus(wallet, versionCode)
        .map { map(it) }
        .onErrorReturn { map(it) }
  }

  private fun map(throwable: Throwable): UserStats {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> UserStats(UserStats.Status.NO_NETWORK)
      else -> {
        UserStats(UserStats.Status.UNKNOWN_ERROR)
      }
    }
  }

  private fun map(response: UserStatusResponse): UserStats {
    val gamification = response.gamification
    return UserStats(UserStats.Status.OK, gamification.level,
        gamification.nextLevelAmount, gamification.bonus, gamification.totalSpend,
        gamification.totalEarned,
        GamificationResponse.Status.ACTIVE == gamification.status)
  }

  override fun getLevels(): Single<Levels> {
    return api.getLevels()
        .map { map(it) }
        .onErrorReturn { mapLevelsError(it) }
  }

  private fun mapLevelsError(throwable: Throwable): Levels {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> Levels(Levels.Status.NO_NETWORK)
      else -> {
        Levels(Levels.Status.UNKNOWN_ERROR)
      }
    }
  }

  private fun map(response: LevelsResponse): Levels {
    val list = mutableListOf<Levels.Level>()
    for (level in response.list) {
      list.add(Levels.Level(level.amount, level.bonus, level.level))
    }
    return Levels(Levels.Status.OK, list.toList(), LevelsResponse.Status.ACTIVE == response.status)
  }

  override fun getUserStatus(wallet: String): Single<UserStatusResponse> {
    return api.getUserStatus(wallet, versionCode)
  }

  override fun getGamificationUserStatus(wallet: String): Single<GamificationResponse> {
    return api.getUserStatus(wallet, versionCode)
        .map { it.gamification }
  }

  override fun getReferralUserStatus(wallet: String): Single<ReferralResponse> {
    return api.getUserStatus(wallet, versionCode)
        .map { it.referral }
  }
}