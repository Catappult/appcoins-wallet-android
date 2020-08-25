package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.*
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.util.concurrent.TimeUnit

class PromotionsInteractor(private val referralInteractor: ReferralInteractorContract,
                           private val gamificationInteractor: GamificationInteractor,
                           private val promotionsRepo: PromotionsRepository,
                           private val findWalletInteract: FindDefaultWalletInteract,
                           private val mapper: GamificationMapper) :
    PromotionsInteractorContract {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
    const val DEFAULT_VIEW_TYPE = "DEFAULT"
    const val PROGRESS_VIEW_TYPE = "PROGRESS"
  }

  override fun retrievePromotions(): Single<PromotionsModel> {
    return findWalletInteract.find()
        .flatMap {
          Single.zip(
              gamificationInteractor.getLevels(),
              promotionsRepo.getUserStatus(it.address),
              BiFunction { level: Levels, userStatsResponse: UserStatusResponse ->
                mapToPromotionsModel(userStatsResponse, level)
              }
          )
              .flatMap { model ->
                setSeenGenericPromotion(model.promotions, it.address,
                    PromotionUpdateScreen.PROMOTIONS)
                    .toSingle { model }
              }
        }

  }

  override fun hasAnyPromotionUpdate(referralsScreen: ReferralsScreen,
                                     gamificationScreen: GamificationScreen,
                                     promotionUpdateScreen: PromotionUpdateScreen): Single<Boolean> {
    return findWalletInteract.find()
        .flatMap { wallet ->
          promotionsRepo.getUserStatus(wallet.address)
              .flatMap {
                val gamification =
                    it.promotions.firstOrNull { promotionsResponse -> promotionsResponse is GamificationResponse } as GamificationResponse?
                val referral =
                    it.promotions.firstOrNull { referralResponse -> referralResponse is ReferralResponse } as ReferralResponse?
                val generic =
                    it.promotions.filter { promotionsResponse -> promotionsResponse !is GamificationResponse && promotionsResponse !is ReferralResponse }
                Single.zip(
                    referralInteractor.hasReferralUpdate(referral, ReferralsScreen.PROMOTIONS),
                    gamificationInteractor.hasNewLevel(gamification, GamificationScreen.PROMOTIONS),
                    hasGenericUpdate(generic, wallet.address, promotionUpdateScreen),
                    Function3 { hasReferralUpdate: Boolean, hasNewLevel: Boolean, hasGenericUpdate: Boolean ->
                      hasReferralUpdate || hasNewLevel || hasGenericUpdate
                    })
              }
        }
  }

  private fun hasGenericUpdate(promotions: List<PromotionsResponse>, wallet: String,
                               screen: PromotionUpdateScreen): Single<Boolean> {
    return Single.create {
      val hasUpdate = promotions.any { promotion ->
        val genericResponse = promotion as GenericResponse
        promotionsRepo.getSeenGenericPromotion(wallet, genericResponse.id + "_" + promotion.endDate,
            screen.name)
            .not()
      }
      it.onSuccess(hasUpdate)
    }
  }

  private fun setSeenGenericPromotion(promotions: List<Promotion>, wallet: String,
                                      screen: PromotionUpdateScreen): Completable {
    return Observable.fromIterable(promotions)
        .flatMapCompletable {
          when (it) {
            is FutureItem -> promotionsRepo.setSeenGenericPromotion(wallet,
                it.id + "_" + it.endDate, screen.name)
            is DefaultItem -> promotionsRepo.setSeenGenericPromotion(wallet,
                it.id + "_" + it.endDate, screen.name)
            is ProgressItem -> promotionsRepo.setSeenGenericPromotion(wallet,
                it.id + "_" + it.endDate, screen.name)
            else -> Completable.complete()
          }
        }
  }

  private fun mapToPromotionsModel(userStatus: UserStatusResponse,
                                   levels: Levels): PromotionsModel {
    var gamificationAvailable = false
    var referralsAvailable = false
    var perksAvailable = false
    val promotions = mutableListOf<Promotion>()
    var maxBonus = 0.0
    userStatus.promotions.sortedByDescending { it.priority }
        .forEach {
          when (it) {
            is GamificationResponse -> {
              gamificationAvailable = it.status == PromotionsResponse.Status.ACTIVE
              promotions.add(mapToGamificationItem(it))

              if (levels.isActive) {
                maxBonus = levels.list.maxBy { level -> level.bonus }?.bonus ?: 0.0
              }

              if (gamificationAvailable) {
                promotions.add(0,
                    TitleItem(R.string.perks_gamif_title, R.string.perks_gamif_subtitle, true,
                        maxBonus.toString()))
              }
            }
            is ReferralResponse -> {
              referralsAvailable = it.status == PromotionsResponse.Status.ACTIVE
              promotions.add(mapToReferralItem(it))
            }
            is GenericResponse -> {
              perksAvailable = true

              when {
                isFuturePromotion(it) -> mapToFutureItem(it)
                it.viewType == PROGRESS_VIEW_TYPE -> promotions.add(mapToProgressItem(it))
                else -> promotions.add(mapToDefaultItem(it))
              }

              if (isValidGamificationLink(it.linkedPromotionId, gamificationAvailable,
                      it.startDate ?: 0)) {
                mapToGamificationLinkItem(promotions, it)
              }
            }
          }
        }

    if (perksAvailable) {
      promotions.add(2,
          TitleItem(R.string.perks_title, R.string.perks_body, false))
    }

    return PromotionsModel(gamificationAvailable, referralsAvailable, promotions, maxBonus,
        userStatus.error)
  }

  private fun mapToGamificationLinkItem(promotions: MutableList<Promotion>,
                                        genericResponse: GenericResponse) {
    val gamificationItem = promotions[1] as GamificationItem
    gamificationItem.links.add(GamificationLinkItem(genericResponse.title, genericResponse.icon))
  }

  private fun mapToProgressItem(genericResponse: GenericResponse): ProgressItem {
    return ProgressItem(genericResponse.id, genericResponse.title, genericResponse.icon,
        genericResponse.endDate, genericResponse.currentProgress!!,
        genericResponse.objectiveProgress!!)
  }

  private fun mapToDefaultItem(genericResponse: GenericResponse): DefaultItem {
    return DefaultItem(genericResponse.id, genericResponse.title, genericResponse.icon,
        genericResponse.endDate)
  }

  private fun mapToGamificationItem(gamificationResponse: GamificationResponse): GamificationItem {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(gamificationResponse.level)

    return GamificationItem(gamificationResponse.id, currentLevelInfo.planet,
        currentLevelInfo.levelColor, currentLevelInfo.title, currentLevelInfo.phrase,
        gamificationResponse.bonus, mutableListOf())
  }

  private fun mapToReferralItem(referralResponse: ReferralResponse): ReferralItem {
    return ReferralItem(referralResponse.id, referralResponse.amount, referralResponse.currency,
        referralResponse.link.orEmpty())
  }

  private fun mapToFutureItem(genericResponse: GenericResponse): FutureItem {
    return FutureItem(genericResponse.id, genericResponse.title, genericResponse.icon,
        genericResponse.endDate)
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

}