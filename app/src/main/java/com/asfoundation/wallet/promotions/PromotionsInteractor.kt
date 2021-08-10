package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.appcoins.wallet.gamification.repository.entity.*
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PromotionsInteractor(private val referralInteractor: ReferralInteractorContract,
                           private val gamificationInteractor: GamificationInteractor,
                           private val promotionsRepo: PromotionsRepository,
                           private val findWalletUseCase: FindDefaultWalletUseCase,
                           private val userStatsPreferencesRepository: UserStatsLocalData,
                           private val analyticsSetup: AnalyticsSetup,
                           private val mapper: GamificationMapper) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val GAMIFICATION_INFO = "GAMIFICATION_INFO"
    const val REFERRAL_ID = "REFERRAL"
    const val VOUCHER_ID = "VOUCHER"
    const val PROGRESS_VIEW_TYPE = "PROGRESS"
  }

  fun retrievePromotions(): Single<PromotionsModel> {
    return findWalletUseCase()
        .flatMap {
          Single.zip(Single.fromObservable(promotionsRepo.getUserStats(it.address)),
              getMockedVouchers(),
              Single.fromObservable(gamificationInteractor.getLevels()),
              Function3 { userStats: UserStats, vouchers: VoucherListModel, level: Levels ->
                analyticsSetup.setWalletOrigin(map(userStats.walletOrigin))
                mapToPromotionsModel(userStats, vouchers, level)
              })
              .doOnSuccess { model ->
                userStatsPreferencesRepository.setSeenWalletOrigin(it.address,
                    model.walletOrigin.name)
                setSeenGenericPromotion(model.promotions, it.address)
              }
        }

  }

  private fun getMockedVouchers(): Single<VoucherListModel> {
    return Single.just(VoucherListModel(listOf(
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            true))))
  }

  fun hasAnyPromotionUpdate(promotionUpdateScreen: PromotionUpdateScreen): Single<Boolean> {
    return findWalletUseCase()
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
              .subscribeOn(Schedulers.io())
        }
  }

  fun getUnwatchedPromotionNotification(): Single<CardNotification> {
    return findWalletUseCase()
        .flatMap { wallet ->
          promotionsRepo.getUserStats(wallet.address)
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
        PromotionNotification(CardNotificationAction.NONE, res.notificationTitle,
            res.notificationDescription, res.icon,
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

  private fun mapToPromotionsModel(userStatus: UserStats,
                                   vouchersListModel: VoucherListModel,
                                   levels: Levels): PromotionsModel {
    val promotions = mutableListOf<Promotion>()
    val perks = mutableListOf<PerkPromotion>()
    val maxBonus = getMaxBonus(levels)
    val vouchers = handleVouchers(vouchersListModel, maxBonus)
    userStatus.promotions.sortedByDescending { it.priority }
        .forEach {
          when (it) {
            is GamificationResponse -> handleGamificationItem(promotions, maxBonus, it)
            is ReferralResponse -> handleReferralItem(promotions, it)
            is GenericResponse -> {
              if (isPerk(it.linkedPromotionId)) handlePerkItem(perks, it)
              else handleGamificationLinkItem(promotions, it)
            }
          }
        }
    return PromotionsModel(promotions, vouchers, perks, map(userStatus.walletOrigin))
  }

  private fun handleVouchers(vouchersListModel: VoucherListModel,
                             maxBonus: Double): List<VoucherItem> {
    val list = ArrayList<VoucherItem>()
    vouchersListModel.vouchers.forEach {
      list.add(VoucherItem(VOUCHER_ID, it.packageName, it.title, it.icon, it.hasAppcoins, maxBonus))
    }
    return list
  }

  private fun handleGamificationItem(promotions: MutableList<Promotion>, maxBonus: Double,
                                     gamificationResponse: GamificationResponse) {
    if (gamificationResponse.status == PromotionsResponse.Status.ACTIVE) {
      promotions.add(mapToGamificationItem(maxBonus, gamificationResponse))
    }
  }

  private fun getMaxBonus(levels: Levels): Double {
    if (levels.isActive) return levels.list.maxBy { level -> level.bonus }?.bonus ?: 0.0
    return 0.0
  }

  private fun handleReferralItem(promotions: MutableList<Promotion>,
                                 referralResponse: ReferralResponse) {
    if (referralResponse.status == PromotionsResponse.Status.ACTIVE) {
      promotions.add(mapToReferralItem(referralResponse))
    }
  }

  private fun handlePerkItem(perks: MutableList<PerkPromotion>, it: GenericResponse) {
    when {
      isFuturePromotion(it) -> perks.add(mapToFutureItem(it))
      it.viewType == PROGRESS_VIEW_TYPE -> perks.add(mapToProgressItem(it))
      else -> perks.add(mapToDefaultItem(it))
    }
  }

  private fun handleGamificationLinkItem(promotions: MutableList<Promotion>,
                                         promotionsResponse: GenericResponse) {
    val gamificationItem = getGamificationItem(promotions)
    if (isValidGamificationLink(promotionsResponse.linkedPromotionId, gamificationItem,
            promotionsResponse.startDate ?: 0)) {
      mapToGamificationLinkItem(getGamificationItem(promotions), promotionsResponse)
    }
  }

  private fun isPerk(linkedPromotionId: String?): Boolean = linkedPromotionId != GAMIFICATION_ID

  private fun getGamificationItem(promotions: MutableList<Promotion>): GamificationItem? {
    for (promotion in promotions) {
      if (promotion is GamificationItem) return promotion
    }
    return null
  }

  private fun map(walletOrigin: WalletOrigin): PromotionsModel.WalletOrigin {
    return when (walletOrigin) {
      WalletOrigin.UNKNOWN -> PromotionsModel.WalletOrigin.UNKNOWN
      WalletOrigin.APTOIDE -> PromotionsModel.WalletOrigin.APTOIDE
      WalletOrigin.PARTNER -> PromotionsModel.WalletOrigin.PARTNER
    }
  }

  private fun mapToGamificationLinkItem(gamificationItem: GamificationItem?,
                                        genericResponse: GenericResponse) {
    gamificationItem?.links?.add(
        GamificationLinkItem(genericResponse.id, genericResponse.notificationDescription!!,
            genericResponse.icon,
            genericResponse.startDate, genericResponse.endDate))
  }

  private fun mapToProgressItem(genericResponse: GenericResponse): ProgressItem {
    return ProgressItem(genericResponse.id, genericResponse.notificationDescription!!,
        genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.currentProgress!!,
        genericResponse.objectiveProgress, genericResponse.detailsLink)
  }

  private fun mapToDefaultItem(genericResponse: GenericResponse): DefaultItem {
    return DefaultItem(genericResponse.id, genericResponse.notificationDescription!!,
        genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.detailsLink)
  }

  private fun mapToGamificationItem(maxBonus: Double,
                                    gamificationResponse: GamificationResponse): GamificationItem {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(gamificationResponse.level)
    val toNextLevelAmount =
        gamificationResponse.nextLevelAmount?.minus(gamificationResponse.totalSpend)

    return GamificationItem(gamificationResponse.id, currentLevelInfo.planet,
        gamificationResponse.level, currentLevelInfo.levelColor, currentLevelInfo.title,
        toNextLevelAmount, gamificationResponse.bonus, maxBonus, mutableListOf())
  }

  private fun mapToReferralItem(referralResponse: ReferralResponse): ReferralItem {
    return ReferralItem(referralResponse.id, referralResponse.amount, referralResponse.currency,
        referralResponse.link.orEmpty())
  }

  private fun mapToFutureItem(genericResponse: GenericResponse): FutureItem {
    return FutureItem(genericResponse.id, genericResponse.notificationDescription!!,
        genericResponse.icon,
        genericResponse.startDate, genericResponse.endDate, genericResponse.detailsLink)
  }

  private fun isValidGamificationLink(linkedPromotionId: String?,
                                      gamificationItem: GamificationItem?,
                                      startDate: Long): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return linkedPromotionId != null && linkedPromotionId == GAMIFICATION_ID &&
        gamificationItem != null && startDate < currentTime
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