package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.Status
import com.appcoins.wallet.gamification.repository.entity.*
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import java.util.concurrent.TimeUnit

class PromotionsInteractor(private val referralInteractor: ReferralInteractorContract,
                           private val gamificationInteractor: GamificationInteractor,
                           private val promotionsRepo: PromotionsRepository,
                           private val findWalletInteract: FindDefaultWalletInteract,
                           private val userStatsPreferencesRepository: UserStatsLocalData,
                           private val analyticsSetup: AnalyticsSetup,
                           private val mapper: GamificationMapper) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
    const val PROGRESS_VIEW_TYPE = "PROGRESS"
  }

  fun retrievePromotions(): Observable<PromotionsModel> {
    return findWalletInteract.find()
        .flatMapObservable {
          Observable.zip(
              gamificationInteractor.getLevels(),
              promotionsRepo.getUserStats(it.address),
              BiFunction { levels: Levels, userStatsResponse: UserStats ->
                if (userStatsResponse.error == null) {
                  analyticsSetup.setWalletOrigin(userStatsResponse.walletOrigin)
                }
                mapToPromotionsModel(userStatsResponse, levels)
              })
              .doOnNext { model ->
                if (model.error == null) {
                  userStatsPreferencesRepository.setSeenWalletOrigin(it.address,
                      model.walletOrigin.name)
                  setSeenGenericPromotion(model.promotions, it.address)
                }
              }
        }
  }

  fun hasAnyPromotionUpdate(promotionUpdateScreen: PromotionUpdateScreen): Single<Boolean> {
    return findWalletInteract.find()
        .flatMap { wallet ->
          promotionsRepo.getUserStats(wallet.address, false)
              .lastOrError()
              .flatMap {
                val gamification =
                    it.promotions.firstOrNull { promotionsResponse -> promotionsResponse is GamificationResponse } as GamificationResponse?
                val referral =
                    it.promotions.firstOrNull { referralResponse -> referralResponse is ReferralResponse } as ReferralResponse?
                val generic = it.promotions.filterIsInstance<GenericResponse>()
                Single.zip(
                    referralInteractor.hasReferralUpdate(wallet.address, referral,
                        ReferralsScreen.PROMOTIONS),
                    gamificationInteractor.hasNewLevel(wallet.address, gamification,
                        GamificationContext.SCREEN_PROMOTIONS),
                    hasGenericUpdate(generic, promotionUpdateScreen),
                    hasNewWalletOrigin(wallet.address, it.walletOrigin),
                    Function4 { hasReferralUpdate: Boolean, hasNewLevel: Boolean,
                                hasGenericUpdate: Boolean, hasNewWalletOrigin: Boolean ->
                      hasReferralUpdate || hasNewLevel || hasGenericUpdate || hasNewWalletOrigin
                    })
              }
        }
  }

  fun getUnwatchedPromotionNotification(): Single<CardNotification> {
    return findWalletInteract.find()
        .flatMap { wallet ->
          promotionsRepo.getUserStats(wallet.address, false)
              .lastOrError()
              .map {
                val promotionList = it.promotions.filterIsInstance<GenericResponse>()
                val unwatchedPromotion = getUnWatchedPromotion(promotionList)
                buildPromotionNotification(unwatchedPromotion)
              }
        }
  }

  fun dismissNotification(id: String): Completable {
    return Completable.fromAction {
      promotionsRepo.setSeenGenericPromotion(id, PromotionUpdateScreen.TRANSACTIONS.name)
    }
  }

  fun shouldShowGamificationDisclaimer(): Boolean {
    return userStatsPreferencesRepository.shouldShowGamificationDisclaimer()
  }

  fun setGamificationDisclaimerShown() =
      userStatsPreferencesRepository.setGamificationDisclaimerShown()

  private fun getUnWatchedPromotion(promotionList: List<GenericResponse>): GenericResponse? {
    return promotionList.sortedByDescending { list -> list.priority }
        .firstOrNull {
          !promotionsRepo.getSeenGenericPromotion(
              getPromotionIdKey(it.id, it.startDate, it.endDate),
              PromotionUpdateScreen.TRANSACTIONS.name)
        }
  }

  private fun buildPromotionNotification(unwatchedPromotion: GenericResponse?): CardNotification {
    return unwatchedPromotion?.let { res ->
      if (!isFuturePromotion(res)) {
        val action = CardNotificationAction.DETAILS_URL.takeIf { res.detailsLink != null }
            ?: CardNotificationAction.NONE
        PromotionNotification(action, res.notificationTitle, res.notificationDescription, res.icon,
            getPromotionIdKey(res.id, res.startDate, res.endDate), res.detailsLink)
      } else {
        EmptyNotification()
      }
    } ?: EmptyNotification()
  }

  private fun hasGenericUpdate(promotions: List<GenericResponse>,
                               screen: PromotionUpdateScreen): Single<Boolean> {
    return Single.create {
      val hasUpdate = promotions.any { promotion ->
        promotionsRepo.getSeenGenericPromotion(
            getPromotionIdKey(promotion.id, promotion.startDate, promotion.endDate),
            screen.name)
            .not()
      }
      it.onSuccess(hasUpdate)
    }
  }

  private fun setSeenGenericPromotion(promotions: List<Promotion>, wallet: String) {
    promotions.forEach {
      when (it) {
        is GamificationItem -> {
          promotionsRepo.shownLevel(wallet, it.level, GamificationContext.SCREEN_PROMOTIONS)
          it.links.forEach { gamificationLinkItem ->
            promotionsRepo.setSeenGenericPromotion(
                getPromotionIdKey(gamificationLinkItem.id, gamificationLinkItem.startDate,
                    gamificationLinkItem.endDate), PromotionUpdateScreen.PROMOTIONS.name)
          }
        }
        is PerkPromotion -> promotionsRepo.setSeenGenericPromotion(
            getPromotionIdKey(it.id, it.startDate, it.endDate),
            PromotionUpdateScreen.PROMOTIONS.name)
      }
    }
  }

  private fun mapToPromotionsModel(userStats: UserStats,
                                   levels: Levels): PromotionsModel {
    var gamificationAvailable = false
    var referralAvailable = false
    var perksAvailable = false
    val promotions = mutableListOf<Promotion>()
    var maxBonus = 0.0
    userStats.promotions.sortedByDescending { it.priority }
        .forEach {
          when (it) {
            is GamificationResponse -> {
              gamificationAvailable = it.status == PromotionsResponse.Status.ACTIVE

              if (levels.isActive) {
                maxBonus = levels.list.maxBy { level -> level.bonus }?.bonus ?: 0.0
              }

              if (gamificationAvailable) {
                promotions.add(0,
                    TitleItem(R.string.perks_gamif_title, R.string.perks_gamif_subtitle, true,
                        maxBonus.toString()))
                promotions.add(1, mapToGamificationItem(it))
              }
            }
            is ReferralResponse -> {
              referralAvailable = it.status == PromotionsResponse.Status.ACTIVE
              if (referralAvailable) {
                val index = if (gamificationAvailable) 2 else 0
                promotions.add(index, mapToReferralItem(it))
              }
            }
            is GenericResponse -> {
              if (it.linkedPromotionId != GAMIFICATION_ID) {
                perksAvailable = true
                when {
                  isFuturePromotion(it) -> promotions.add(mapToFutureItem(it))
                  it.viewType == PROGRESS_VIEW_TYPE -> promotions.add(mapToProgressItem(it))
                  else -> promotions.add(mapToDefaultItem(it))
                }
              }
              if (isValidGamificationLink(it.linkedPromotionId, gamificationAvailable,
                      it.startDate ?: 0)) {
                mapToGamificationLinkItem(promotions, it)
              }
            }
          }
        }

    if (perksAvailable) {
      val perksIndex = getPerksIndex(gamificationAvailable, referralAvailable)
      promotions.add(perksIndex, TitleItem(R.string.perks_title, R.string.perks_body, false))
    }

    return PromotionsModel(promotions, maxBonus, map(userStats.walletOrigin), map(userStats.error),
        levels.fromCache && userStats.fromCache)
  }

  private fun getPerksIndex(gamificationAvailable: Boolean, referralAvailable: Boolean): Int {
    return when {
      gamificationAvailable && referralAvailable -> 3
      gamificationAvailable && !referralAvailable -> 2
      !gamificationAvailable && referralAvailable -> 1
      else -> 0
    }
  }

  private fun map(walletOrigin: WalletOrigin): com.asfoundation.wallet.promotions.WalletOrigin {
    return when (walletOrigin) {
      WalletOrigin.UNKNOWN -> com.asfoundation.wallet.promotions.WalletOrigin.UNKNOWN
      WalletOrigin.APTOIDE -> com.asfoundation.wallet.promotions.WalletOrigin.APTOIDE
      WalletOrigin.PARTNER -> com.asfoundation.wallet.promotions.WalletOrigin.PARTNER
    }
  }

  private fun map(error: Status?): com.asfoundation.wallet.promotions.Status? {
    return when (error) {
      null -> null
      Status.NO_NETWORK -> com.asfoundation.wallet.promotions.Status.NO_NETWORK
      Status.UNKNOWN_ERROR -> com.asfoundation.wallet.promotions.Status.UNKNOWN_ERROR
    }
  }

  private fun mapToGamificationLinkItem(promotions: MutableList<Promotion>,
                                        genericResponse: GenericResponse) {
    val gamificationItem = promotions[1] as GamificationItem
    gamificationItem.links.add(
        GamificationLinkItem(genericResponse.id, genericResponse.perkDescription,
            genericResponse.icon, genericResponse.startDate, genericResponse.endDate))
  }

  private fun mapToProgressItem(genericResponse: GenericResponse): ProgressItem {
    return ProgressItem(genericResponse.id, genericResponse.perkDescription, genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.currentProgress!!,
        genericResponse.objectiveProgress, genericResponse.detailsLink)
  }

  private fun mapToDefaultItem(genericResponse: GenericResponse): DefaultItem {
    return DefaultItem(genericResponse.id, genericResponse.perkDescription, genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.detailsLink)
  }

  private fun mapToGamificationItem(
      gamificationResponse: GamificationResponse): GamificationItem {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(gamificationResponse.level)
    val toNextLevelAmount =
        gamificationResponse.nextLevelAmount?.minus(gamificationResponse.totalSpend)

    return GamificationItem(gamificationResponse.id, currentLevelInfo.planet,
        gamificationResponse.level, currentLevelInfo.levelColor, currentLevelInfo.title,
        toNextLevelAmount, gamificationResponse.bonus, mutableListOf())
  }

  private fun mapToReferralItem(referralResponse: ReferralResponse): ReferralItem {
    return ReferralItem(referralResponse.id, referralResponse.amount, referralResponse.currency,
        referralResponse.link.orEmpty())
  }

  private fun mapToFutureItem(genericResponse: GenericResponse): FutureItem {
    return FutureItem(genericResponse.id, genericResponse.perkDescription, genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.detailsLink)
  }

  private fun isValidGamificationLink(linkedPromotionId: String?,
                                      gamificationAvailable: Boolean, startDate: Long): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return linkedPromotionId != null && linkedPromotionId == GAMIFICATION_ID && gamificationAvailable && startDate < currentTime
  }

  private fun isFuturePromotion(genericResponse: GenericResponse): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return genericResponse.startDate ?: 0 > currentTime
  }

  private fun hasNewWalletOrigin(address: String, walletOrigin: WalletOrigin): Single<Boolean> {
    return Single.just(
        userStatsPreferencesRepository.getSeenWalletOrigin(address) != walletOrigin.name)
  }

  private fun getPromotionIdKey(id: String, startDate: Long?, endDate: Long): String {
    return id + "_" + startDate + "_" + endDate
  }
}