package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.core.network.backend.model.GamificationResponse
import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException
import javax.inject.Inject

class Gamification @Inject constructor(private val repository: PromotionsRepository) {
//
  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
  }

  fun getUserStats(wallet: String, promoCodeString: String?): Observable<PromotionsGamificationStats> {
    return repository.getGamificationStats(wallet, promoCodeString)
  }

  fun getUserLevel(wallet: String, promoCodeString: String?): Single<Int> {
    return repository.getGamificationLevel(wallet, promoCodeString)
  }

  fun getLevels(wallet: String, offlineFirst: Boolean = true): Observable<Levels> {
    return repository.getLevels(wallet, offlineFirst)
  }

  fun getUserBonusAndLevel(wallet: String,
                           promoCodeString: String?): Single<ForecastBonusAndLevel> {
    return repository.getUserStats(wallet, promoCodeString, false)
        .map { map(it) }
        .lastOrError()
        .onErrorReturn { mapReferralError(it) }
  }

  private fun map(userStats: UserStats): ForecastBonusAndLevel {
    val gamification = userStats.promotions
        .firstOrNull {
          it is GamificationResponse && it.id == GAMIFICATION_ID
        } as GamificationResponse?

    val referral = userStats.promotions
        .firstOrNull {
          it is GamificationResponse && it.id == REFERRAL_ID
        } as ReferralResponse?

    return if (referral == null || referral.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
      ForecastBonusAndLevel(status = ForecastBonus.Status.INACTIVE,
          level = gamification?.level ?: 0)
    } else {
      ForecastBonusAndLevel(
          ForecastBonus.Status.ACTIVE,
          referral.pendingAmount,
          minAmount = referral.minAmount,
          level = gamification?.level ?: 0)
    }
  }

  private fun mapReferralError(throwable: Throwable): ForecastBonusAndLevel {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> ForecastBonusAndLevel(ForecastBonus.Status.NO_NETWORK)
      else -> {
        ForecastBonusAndLevel(ForecastBonus.Status.UNKNOWN_ERROR)
      }
    }
  }

  fun getEarningBonus(wallet: String, packageName: String,
                      amount: BigDecimal, promoCodeString: String?): Single<ForecastBonus> {
    return repository.getForecastBonus(wallet, packageName, amount, promoCodeString)
  }

  fun hasNewLevel(wallet: String, gamificationContext: GamificationContext,
                  level: Int): Single<Boolean> {
    return repository.getLastShownLevel(wallet, gamificationContext)
        .map { lastShownLevel: Int -> lastShownLevel < level }
  }

  fun levelShown(wallet: String, level: Int, gamificationContext: GamificationContext):
      Completable {
    return Completable.fromAction { repository.shownLevel(wallet, level, gamificationContext) }
  }

}
