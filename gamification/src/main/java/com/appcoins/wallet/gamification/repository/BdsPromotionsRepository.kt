package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.entity.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.IOException
import java.math.BigDecimal
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit

class BdsPromotionsRepository(private val api: GamificationApi,
                              private val local: UserStatsLocalData) : PromotionsRepository {

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromResponses(wallet: String,
                                        offlineFirst: Boolean = true): Observable<UserStats> {
    return if (offlineFirst) Observable.concat(getUserStatsFromDB(wallet),
        getUserStatsFromAPI(wallet))
    else getUserStatsFromAPI(wallet, true)
  }

  // NOTE: the use of the throwable parameter can be dropped once all usages in these repository
  //  follow offline first logic.
  private fun getUserStatsFromDB(wallet: String,
                                 throwable: Throwable? = null): Observable<UserStats> {
    return Single.zip(local.getPromotions(), local.retrieveWalletOrigin(wallet),
        BiFunction { promotions: List<PromotionsResponse>, walletOrigin: WalletOrigin ->
          Pair(promotions, walletOrigin)
        })
        .toObservable()
        .map { (promotions, walletOrigin) ->
          if (throwable == null) UserStats(promotions, walletOrigin, null, true)
          else mapErrorToUserStatsModel(promotions, walletOrigin, throwable)
        }
        .onErrorReturn {
          mapErrorToUserStatsModel(throwable ?: it, throwable == null)
        }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromAPI(wallet: String,
                                  useDbOnError: Boolean = false): Observable<UserStats> {
    return api.getUserStats(wallet, Locale.getDefault().language)
        .map { filterByDate(it) }
        .flatMapObservable {
          local.deleteAndInsertPromotions(it.promotions)
              .andThen(local.insertWalletOrigin(wallet, it.walletOrigin))
              .toSingle { UserStats(it.promotions, it.walletOrigin) }
              .toObservable()
        }
        .onErrorResumeNext { throwable: Throwable ->
          if (useDbOnError) getUserStatsFromDB(wallet, throwable)
          else Observable.just(mapErrorToUserStatsModel(throwable, false))
        }
  }

  private fun filterByDate(userStatusResponse: UserStatusResponse): UserStatusResponse {
    val validPromotions = userStatusResponse.promotions.filter { hasValidDate(it) }
    return UserStatusResponse(validPromotions, userStatusResponse.walletOrigin)
  }

  private fun hasValidDate(promotionsResponse: PromotionsResponse): Boolean {
    return if (promotionsResponse is GenericResponse) {
      val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      currentTime < promotionsResponse.endDate
    } else true
  }

  override fun getLastShownLevel(wallet: String,
                                 gamificationContext: GamificationContext): Single<Int> {
    return local.getLastShownLevel(wallet, gamificationContext)
  }

  override fun shownLevel(wallet: String, level: Int, gamificationContext: GamificationContext) {
    return local.saveShownLevel(wallet, level, gamificationContext)
  }

  override fun getSeenGenericPromotion(id: String, screen: String): Boolean {
    return local.getSeenGenericPromotion(id, screen)
  }

  override fun setSeenGenericPromotion(id: String, screen: String) {
    return local.setSeenGenericPromotion(id, screen)
  }

  override fun getForecastBonus(wallet: String, packageName: String,
                                amount: BigDecimal): Single<ForecastBonus> {
    return api.getForecastBonus(wallet, packageName, amount, "APPC")
        .map { map(it) }
        .onErrorReturn { mapForecastError(it) }
  }

  private fun mapForecastError(throwable: Throwable): ForecastBonus {
    throwable.printStackTrace()
    return if (isNoNetworkException(throwable)) {
      ForecastBonus(ForecastBonus.Status.NO_NETWORK)
    } else {
      ForecastBonus(ForecastBonus.Status.UNKNOWN_ERROR)
    }
  }

  private fun map(bonusResponse: ForecastBonusResponse): ForecastBonus {
    if (bonusResponse.status == ForecastBonusResponse.Status.ACTIVE) {
      return ForecastBonus(ForecastBonus.Status.ACTIVE, bonusResponse.bonus)
    }
    return ForecastBonus(ForecastBonus.Status.INACTIVE)
  }

  override fun getGamificationStats(wallet: String): Observable<GamificationStats> {
    return getUserStatsFromResponses(wallet)
        .map {
          val gamificationStats = mapToGamificationStats(it)
          if (!it.fromCache && it.error == null) local.setGamificationLevel(gamificationStats.level)
          gamificationStats
        }
  }

  override fun getGamificationLevel(wallet: String): Single<Int> {
    return getUserStats(wallet)
        .filter { it.error == null }
        .map { mapToGamificationStats(it).level }
        .lastOrError()
        .onErrorReturn { GamificationStats.INVALID_LEVEL }
  }

  private fun map(status: Status, fromCache: Boolean = false): GamificationStats {
    return if (status == Status.NO_NETWORK) {
      GamificationStats(GamificationStats.Status.NO_NETWORK, fromCache = fromCache)
    } else {
      GamificationStats(GamificationStats.Status.UNKNOWN_ERROR, fromCache = fromCache)
    }
  }

  private fun mapErrorToUserStatsModel(throwable: Throwable, fromCache: Boolean): UserStats {
    return if (isNoNetworkException(throwable)) {
      UserStats(Status.NO_NETWORK, fromCache)
    } else {
      UserStats(Status.UNKNOWN_ERROR, fromCache)
    }
  }

  private fun mapErrorToUserStatsModel(promotions: List<PromotionsResponse>,
                                       walletOrigin: WalletOrigin,
                                       throwable: Throwable): UserStats {
    return when {
      promotions.isEmpty() && isNoNetworkException(throwable) -> {
        throwable.printStackTrace()
        UserStats(Status.NO_NETWORK)
      }
      promotions.isEmpty() -> {
        throwable.printStackTrace()
        UserStats(Status.UNKNOWN_ERROR)
      }
      else -> UserStats(promotions, walletOrigin)
    }
  }

  private fun mapToGamificationStats(stats: UserStats): GamificationStats {
    return if (stats.error != null) {
      map(stats.error, stats.fromCache)
    } else {
      val gamification =
          stats.promotions.firstOrNull { it is GamificationResponse } as GamificationResponse?
      if (gamification == null) {
        GamificationStats(GamificationStats.Status.UNKNOWN_ERROR, fromCache = stats.fromCache)
      } else {
        GamificationStats(GamificationStats.Status.OK, gamification.level,
            gamification.nextLevelAmount, gamification.bonus, gamification.totalSpend,
            gamification.totalEarned, PromotionsResponse.Status.ACTIVE == gamification.status,
            stats.fromCache)
      }
    }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  override fun getLevels(wallet: String, offlineFirst: Boolean): Observable<Levels> {
    return if (offlineFirst) Observable.concat(getLevelsFromDB(), getLevelsFromAPI(wallet))
    else getLevelsFromAPI(wallet, true)
  }

  // NOTE: the use of the throwable parameter can be dropped once all usages in these repository
  //  follow offline first logic.
  private fun getLevelsFromDB(throwable: Throwable? = null): Observable<Levels> {
    return local.getLevels()
        .toObservable()
        .map { map(it, true) }
        .onErrorReturn { mapLevelsError(throwable ?: it, throwable == null) }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getLevelsFromAPI(wallet: String, useDbOnError: Boolean = false): Observable<Levels> {
    return api.getLevels(wallet)
        .flatMapObservable {
          local.deleteLevels()
              .andThen(local.insertLevels(it))
              .toSingle { map(it) }
              .toObservable()
        }
        .onErrorResumeNext { throwable: Throwable ->
          if (useDbOnError) getLevelsFromDB(throwable)
          else Observable.just(mapLevelsError(throwable))
        }
  }

  private fun mapLevelsError(throwable: Throwable, fromCache: Boolean = false): Levels {
    throwable.printStackTrace()
    return if (isNoNetworkException(throwable)) {
      Levels(Levels.Status.NO_NETWORK, fromCache = fromCache)
    } else {
      Levels(Levels.Status.UNKNOWN_ERROR, fromCache = fromCache)
    }
  }

  private fun map(response: LevelsResponse, fromCache: Boolean = false): Levels {
    val list = response.list.map { Levels.Level(it.amount, it.bonus, it.level) }
    return Levels(Levels.Status.OK, list, LevelsResponse.Status.ACTIVE == response.status,
        response.updateDate, fromCache)
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  override fun getUserStats(wallet: String, offlineFirst: Boolean): Observable<UserStats> {
    return getUserStatsFromResponses(wallet, offlineFirst)
        .flatMap { userStatusResponse ->
          val gamification =
              userStatusResponse.promotions.firstOrNull { it is GamificationResponse } as GamificationResponse?
          if (userStatusResponse.error == null && !userStatusResponse.fromCache) {
            local.setGamificationLevel(gamification?.level ?: GamificationStats.INVALID_LEVEL)
          }
          Observable.just(userStatusResponse)
        }
        .doOnError { it.printStackTrace() }
  }

  override fun getWalletOrigin(wallet: String): Single<WalletOrigin> {
    return getUserStats(wallet)
        .filter { it.error == null }
        .map { it.walletOrigin }
        .lastOrError()
        .onErrorReturn { WalletOrigin.UNKNOWN }
  }

  override fun getReferralUserStatus(wallet: String): Single<ReferralResponse> {
    return getUserStatsFromResponses(wallet, false)
        .lastOrError()
        .flatMap {
          val gamification =
              it.promotions.firstOrNull { promotions -> promotions is GamificationResponse } as GamificationResponse?
          val referral =
              it.promotions.firstOrNull { promotions -> promotions is ReferralResponse } as ReferralResponse?
          if (gamification != null) {
            local.setGamificationLevel(gamification.level)
          }
          Single.just(referral)
        }
        .map { it }
  }

  override fun getReferralInfo(): Single<ReferralResponse> {
    return api.getReferralInfo()
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException ||
        throwable.cause != null && throwable.cause is IOException ||
        throwable is UnknownHostException
  }

}