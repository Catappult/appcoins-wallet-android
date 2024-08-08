package com.asfoundation.wallet.promotions

import com.appcoins.wallet.core.network.backend.model.GamificationResponse
import com.appcoins.wallet.core.network.backend.model.GenericResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PromotionsInteractor
@Inject
constructor(
  private val gamificationInteractor: GamificationInteractor,
  private val promotionsRepo: PromotionsRepository,
  private val findWalletUseCase: FindDefaultWalletUseCase,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val userStatsPreferencesRepository: UserStatsLocalData,
) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
    const val VOUCHER_ID = "VOUCHER"
    const val PROGRESS_VIEW_TYPE = "PROGRESS"
    const val PROMO_CODE_PERK = "PROMO_CODE_PERK"
  }

  fun hasAnyPromotionUpdate(promotionUpdateScreen: PromotionUpdateScreen): Single<Boolean> {
    return getCurrentPromoCodeUseCase().flatMap { promoCode ->
      findWalletUseCase().flatMap { wallet ->
        promotionsRepo
          .getUserStats(wallet.address, promoCode.code, false)
          .lastOrError()
          .flatMap {
            val gamification =
              it.promotions.firstOrNull { promotionsResponse ->
                promotionsResponse is GamificationResponse
              } as GamificationResponse?
            val generic = it.promotions.filterIsInstance<GenericResponse>()
            Single.zip(
              gamificationInteractor.hasNewLevel(
                wallet.address, gamification, GamificationContext.SCREEN_PROMOTIONS
              ),
              hasGenericUpdate(generic, promotionUpdateScreen),
              hasNewWalletOrigin(wallet.address, it.walletOrigin)
            ) { hasNewLevel: Boolean,
                hasGenericUpdate: Boolean,
                hasNewWalletOrigin: Boolean ->
              hasNewLevel || hasGenericUpdate || hasNewWalletOrigin
            }
          }
          .subscribeOn(Schedulers.io())
      }
    }
  }

  fun getUnwatchedPromotionNotification(): Single<CardNotification> {
    return getCurrentPromoCodeUseCase().flatMap { promoCode ->
      findWalletUseCase().flatMap { wallet ->
        promotionsRepo.getUserStats(wallet.address, promoCode.code).lastOrError().map {
          val promotionList = it.promotions.filterIsInstance<GenericResponse>()
          val unwatchedPromotion = getUnWatchedPromotion(promotionList)
          buildPromotionNotification(unwatchedPromotion)
        }
      }
    }
  }

  private fun getUnWatchedPromotion(promotionList: List<GenericResponse>): GenericResponse? {
    return promotionList
      .sortedByDescending { list -> list.priority }
      .firstOrNull {
        !promotionsRepo.getSeenGenericPromotion(
          getPromotionIdKey(it.id, it.startDate, it.endDate),
          PromotionUpdateScreen.TRANSACTIONS.name
        )
      }
  }

  private fun buildPromotionNotification(unwatchedPromotion: GenericResponse?): CardNotification {
    return unwatchedPromotion?.let { res ->
      if (!isFuturePromotion(res)) {
        PromotionNotification(
          CardNotificationAction.NONE,
          res.gamificationStatus.toString()
        )
      } else {
        EmptyNotification()
      }
    } ?: EmptyNotification()
  }

  private fun hasGenericUpdate(
    promotions: List<GenericResponse>,
    screen: PromotionUpdateScreen
  ): Single<Boolean> {
    return Single.create {
      val hasUpdate =
        promotions.any { promotion ->
          promotionsRepo
            .getSeenGenericPromotion(
              getPromotionIdKey(promotion.id, promotion.startDate, promotion.endDate),
              screen.name
            )
            .not()
        }
      it.onSuccess(hasUpdate)
    }
  }

  private fun isFuturePromotion(genericResponse: GenericResponse): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return (genericResponse.startDate ?: 0) > currentTime
  }

  private fun hasNewWalletOrigin(address: String, walletOrigin: WalletOrigin): Single<Boolean> {
    return Single.just(
      userStatsPreferencesRepository.getSeenWalletOrigin(address) != walletOrigin.name
    )
  }

  private fun getPromotionIdKey(id: String, startDate: Long?, endDate: Long): String {
    return id + "_" + startDate + "_" + endDate
  }
}
