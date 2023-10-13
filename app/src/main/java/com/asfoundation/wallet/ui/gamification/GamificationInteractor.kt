package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.core.network.backend.model.GamificationResponse
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class GamificationInteractor @Inject constructor(
  private val gamification: Gamification,
  private val defaultWallet: FindDefaultWalletInteract,
  private val conversionService: LocalCurrencyConversionService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  private var isBonusActiveAndValid: Boolean = false

  fun getLevels(offlineFirst: Boolean = true): Observable<Levels> {
    return defaultWallet.find()
      .flatMapObservable { gamification.getLevels(it.address, offlineFirst) }
  }

  fun getUserStats(): Observable<PromotionsGamificationStats> {
    return getCurrentPromoCodeUseCase()
      .flatMapObservable { promoCode ->
        defaultWallet.find()
          .flatMapObservable { gamification.getUserStats(it.address, promoCode.code) }
      }
  }

  fun getUserLevel(): Single<Int> {
    return getCurrentPromoCodeUseCase()
      .flatMap { promoCode ->
        defaultWallet.find()
          .flatMap { gamification.getUserLevel(it.address, promoCode.code) }
      }
  }

  fun getEarningBonus(
    packageName: String, amount: BigDecimal,
    promoCodeString: String?, currency: String?
  ): Single<ForecastBonusAndLevel> {
    return defaultWallet.find()
      .flatMap { wallet: Wallet ->
        gamification.getEarningBonus(
          wallet.address, packageName, amount,
          promoCodeString, currency
        ).map { forecastBonus ->
          ForecastBonusAndLevel(
            forecastBonus.status, forecastBonus.amount, forecastBonus.currency, level = forecastBonus.level
          )
        }
      }.doOnSuccess { isBonusActiveAndValid = isBonusActiveAndValid(it) }
  }

  fun hasNewLevel(
    walletAddress: String,
    gamificationResponse: GamificationResponse?,
    gamificationContext: GamificationContext
  ): Single<Boolean> {
    return if (gamificationResponse == null || gamificationResponse.status != PromotionsResponse.Status.ACTIVE) {
      Single.just(false)
    } else {
      gamification.hasNewLevel(walletAddress, gamificationContext, gamificationResponse.level)
    }
  }

  fun levelShown(level: Int, gamificationContext: GamificationContext): Completable {
    return defaultWallet.find()
      .flatMapCompletable { gamification.levelShown(it.address, level, gamificationContext) }
  }

  fun getAppcToLocalFiat(
    value: String, scale: Int,
    getFromCache: Boolean = false
  ): Single<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, scale, getFromCache)
        .onErrorReturn {
          FiatValue(
            BigDecimal("-1"),
            "",
            ""
          )
        }
  }

  fun isBonusActiveAndValid(): Boolean {
    return isBonusActiveAndValid
  }

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel): Boolean {
    return forecastBonus.status == ForecastBonus.Status.ACTIVE && forecastBonus.amount > BigDecimal.ZERO
  }
}
