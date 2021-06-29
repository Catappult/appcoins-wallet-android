package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.Status
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.*
import com.asf.wallet.R
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import java.util.concurrent.TimeUnit

class PromotionsMapper(private val gamificationMapper: GamificationMapper) {

  fun mapToPromotionsModel(userStats: UserStats,
                           levels: Levels,
                           wallet: Wallet): PromotionsModel {
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
              if (it.linkedPromotionId != PromotionsInteractor.GAMIFICATION_ID) {
                perksAvailable = true
                when {
                  isFuturePromotion(it) -> promotions.add(mapToFutureItem(it))
                  it.viewType == PromotionsInteractor.PROGRESS_VIEW_TYPE -> promotions.add(
                      mapToProgressItem(it))
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

    return PromotionsModel(promotions, maxBonus, wallet, map(userStats.walletOrigin),
        map(userStats.error), levels.fromCache && userStats.fromCache)
  }

  private fun getPerksIndex(gamificationAvailable: Boolean, referralAvailable: Boolean): Int {
    return when {
      gamificationAvailable && referralAvailable -> 3
      gamificationAvailable && !referralAvailable -> 2
      !gamificationAvailable && referralAvailable -> 1
      else -> 0
    }
  }

  private fun map(
      walletOrigin: WalletOrigin): PromotionsModel.WalletOrigin {
    return when (walletOrigin) {
      WalletOrigin.UNKNOWN -> PromotionsModel.WalletOrigin.UNKNOWN
      WalletOrigin.APTOIDE -> PromotionsModel.WalletOrigin.APTOIDE
      WalletOrigin.PARTNER -> PromotionsModel.WalletOrigin.PARTNER
    }
  }

  private fun map(error: Status?): PromotionsModel.Status? {
    return when (error) {
      null -> null
      Status.NO_NETWORK -> PromotionsModel.Status.NO_NETWORK
      Status.UNKNOWN_ERROR -> PromotionsModel.Status.UNKNOWN_ERROR
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
    val currentLevelInfo = gamificationMapper.mapCurrentLevelInfo(gamificationResponse.level)
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
    return linkedPromotionId != null && linkedPromotionId == PromotionsInteractor.GAMIFICATION_ID && gamificationAvailable && startDate < currentTime
  }

  private fun isFuturePromotion(genericResponse: GenericResponse): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return genericResponse.startDate ?: 0 > currentTime
  }
}