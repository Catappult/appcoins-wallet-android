package com.asfoundation.wallet.ui.gamification

import android.content.SharedPreferences
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.*
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.REFERRAL_ID
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import it.czerwinski.android.hilt.annotations.BoundTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@BoundTo(supertype = UserStatsLocalData::class)
class SharedPreferencesUserStatsLocalData @Inject constructor(
  private val preferences: SharedPreferences,
  private val promotionDao: PromotionDao,
  private val levelsDao: LevelsDao,
  private val levelDao: LevelDao,
  private val walletOriginDao: WalletOriginDao
) :
  UserStatsLocalData {

  companion object {
    private const val SHOWN_LEVEL = "shown_level"
    private const val SHOWN_GENERIC = "shown_generic"
    private const val SCREEN = "screen_"
    private const val ID = "id_"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SHOW_GAMIFICATION_DISCLAIMER = "SHOW_GAMIFICATION_DISCLAIMER"
    private const val WALLET_ORIGIN = "wallet_origin"
    private const val VIP_CALLOUT_SEEN = "vip_callout_seen"
  }

  override fun getLastShownLevel(wallet: String, gamificationContext: GamificationContext):
      Single<Int> {
    return Single.fromCallable {
      preferences.getInt(
        getKey(wallet, gamificationContext.toString()),
        PromotionsGamificationStats.INVALID_LEVEL
      )
    }
  }

  override fun saveShownLevel(
    wallet: String, level: Int,
    gamificationContext: GamificationContext
  ) {
    return preferences.edit()
      .putInt(getKey(wallet, gamificationContext.toString()), level)
      .apply()
  }

  override fun getSeenGenericPromotion(id: String, screen: String): Boolean {
    return preferences.getBoolean(getKeyGeneric(screen, id), false)
  }

  override fun setSeenGenericPromotion(id: String, screen: String) {
    return preferences.edit()
      .putBoolean(getKeyGeneric(screen, id), true)
      .apply()
  }

  override fun setGamificationLevel(gamificationLevel: Int) {
    return preferences.edit()
      .putInt(GAMIFICATION_LEVEL, gamificationLevel)
      .apply()
  }

  override fun getGamificationLevel() =
    preferences.getInt(GAMIFICATION_LEVEL, PromotionsGamificationStats.INVALID_LEVEL)

  private fun getKey(wallet: String, screen: String): String {
    return if (screen == GamificationContext.SCREEN_MY_LEVEL.toString()) {
      SHOWN_LEVEL + wallet
    } else {
      SHOWN_LEVEL + wallet + SCREEN + screen
    }
  }

  private fun getKeyGeneric(screen: String, id: String) =
    SHOWN_GENERIC + SCREEN + screen + ID + id

  override fun getPromotions(): Single<List<PromotionsResponse>> {
    return promotionDao.getPromotions()
      .map { filterByDate(it) }
      .map { mapToPromotionResponse(it) }
  }

  private fun filterByDate(promotions: List<PromotionEntity>): List<PromotionEntity> {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return promotions.filter { it.endDate == null || currentTime < it.endDate!! }
  }

  private fun mapToPromotionResponse(promotions: List<PromotionEntity>): List<PromotionsResponse> {
    return promotions.map {
      when (it.id) {
        GAMIFICATION_ID ->
          GamificationResponse(
            it.id,
            it.priority,
            GamificationStatus.toEnum(it.gamificationStatus),
            it.bonus!!,
            it.totalSpend!!,
            it.totalEarned!!,
            it.level!!,
            it.nextLevelAmount,
            it.status!!,
            it.bundle!!
          )
        REFERRAL_ID -> ReferralResponse(
          it.id,
          it.priority,
          GamificationStatus.toEnum(it.gamificationStatus),
          it.maxAmount!!,
          it.available!!,
          it.bundle!!,
          it.completed!!,
          it.currency!!,
          it.symbol!!,
          it.invited!!,
          it.link,
          it.pendingAmount!!,
          it.receivedAmount!!,
          it.userStatus,
          it.minAmount!!,
          it.status!!,
          it.amount!!
        )
        else ->
          GenericResponse(
            id = it.id,
            priority = it.priority,
            gamificationStatus = GamificationStatus.toEnum(it.gamificationStatus),
            currentProgress = it.currentProgress,
            notificationDescription = it.notificationDescription,
            perkDescription = it.perkDescription,
            appName = it.appName,
            endDate = it.endDate!!,
            icon = it.icon,
            linkedPromotionId = it.linkedPromotionId,
            objectiveProgress = it.objectiveProgress,
            startDate = it.startDate,
            notificationTitle = it.notificationTitle!!,
            viewType = it.viewType!!,
            detailsLink = it.detailsLink
          )
      }
    }
  }

  override fun deleteAndInsertPromotions(promotions: List<PromotionsResponse>): Completable {
    return Single.create<List<PromotionEntity>> { emitter ->
      val results: List<PromotionEntity> = promotions.map {
        when (it) {
          is GamificationResponse ->
            PromotionEntity(
              id = it.id,
              priority = it.priority,
              gamificationStatus = it.gamificationStatus.toString(),
              bonus = it.bonus,
              totalSpend = it.totalSpend,
              totalEarned = it.totalEarned,
              level = it.level,
              nextLevelAmount = it.nextLevelAmount,
              status = it.status,
              bundle = it.bundle
            )
          is ReferralResponse -> {
            PromotionEntity(
              id = it.id,
              priority = it.priority,
              gamificationStatus = it.gamificationStatus.toString(),
              maxAmount = it.maxAmount,
              available = it.available,
              bundle = it.bundle,
              completed = it.completed,
              currency = it.currency,
              symbol = it.symbol,
              invited = it.invited,
              link = it.link,
              pendingAmount = it.pendingAmount,
              receivedAmount = it.receivedAmount,
              userStatus = it.userStatus,
              minAmount = it.minAmount,
              status = it.status,
              amount = it.amount
            )
          }
          else -> {
            val genericResponse = it as GenericResponse
            PromotionEntity(
              id = genericResponse.id, priority = genericResponse.priority,
              gamificationStatus = it.gamificationStatus.toString(),
              currentProgress = genericResponse.currentProgress,
              notificationDescription = genericResponse.notificationDescription,
              perkDescription = genericResponse.perkDescription,
              appName = genericResponse.appName,
              endDate = genericResponse.endDate, icon = genericResponse.icon,
              linkedPromotionId = genericResponse.linkedPromotionId,
              objectiveProgress = genericResponse.objectiveProgress,
              startDate = genericResponse.startDate,
              notificationTitle = genericResponse.notificationTitle,
              viewType = genericResponse.viewType, detailsLink = genericResponse.detailsLink
            )
          }
        }
      }
      emitter.onSuccess(results)
    }
      .flatMapCompletable { Completable.fromAction { promotionDao.deleteAndInsert(it) } }
  }

  override fun deleteLevels(): Completable {
    return levelDao.deleteLevels()
      .andThen(levelsDao.deleteLevels())
  }

  override fun getLevels(): Single<LevelsResponse> {
    return Single.zip(
      levelDao.getLevels(),
      levelsDao.getLevels(),
      BiFunction { levelList, levels -> mapToLevelsResponse(levelList, levels) }
    )
  }

  override fun insertLevels(levels: LevelsResponse): Completable {
    val levelsEntity = LevelsEntity(null, levels.status, levels.updateDate)
    val levelEntityList =
      levels.list.map { LevelEntity(null, it.amount, it.bonus, it.level) }
    return levelsDao.insertLevels(levelsEntity)
      .andThen(levelDao.insertLevels(levelEntityList))
  }

  override fun shouldShowGamificationDisclaimer() = preferences.getBoolean(
    SHOW_GAMIFICATION_DISCLAIMER, true
  )

  override fun setGamificationDisclaimerShown() {
    preferences.edit()
      .putBoolean(SHOW_GAMIFICATION_DISCLAIMER, false)
      .apply()
  }

  override fun insertWalletOrigin(wallet: String, walletOrigin: WalletOrigin): Completable {
    return walletOriginDao.insertWalletOrigin(WalletOriginEntity(wallet, walletOrigin))
  }

  override fun retrieveWalletOrigin(wallet: String): Single<WalletOrigin> {
    return walletOriginDao.getWalletOrigin(wallet)
      .map { it.walletOrigin }
  }

  override fun setSeenWalletOrigin(wallet: String, walletOrigin: String) {
    return preferences.edit()
      .putString(WALLET_ORIGIN + wallet, walletOrigin)
      .apply()
  }

  override fun getSeenWalletOrigin(wallet: String): String {
    return preferences.getString(WALLET_ORIGIN + wallet, "")!!
  }

  override fun isVipCalloutAlreadySeen() =
    preferences.getBoolean(VIP_CALLOUT_SEEN, false)

  override fun setVipCalloutAlreadySeen(isSeen: Boolean) {
    preferences.edit()
      .putBoolean(VIP_CALLOUT_SEEN, isSeen)
      .apply()
  }

  private fun mapToLevelsResponse(
    levelEntity: List<LevelEntity>,
    levelsEntity: LevelsEntity
  ): LevelsResponse {
    val levels = levelEntity.map { Level(it.amount, it.bonus, it.level) }
    return LevelsResponse(levels, levelsEntity.status, levelsEntity.updateDate)
  }
}