package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.core.network.backend.model.GamificationResponse
import com.appcoins.wallet.core.network.backend.model.GenericResponse
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.ISO_8601_DATE_TIME_FORMAT
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.transformDateToTimestampSeconds
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.Status
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PromotionsMapper @Inject constructor(private val gamificationMapper: GamificationMapper) {

  fun mapToPromotionsModel(
    userStats: UserStats,
    levels: Levels,
    wallet: Wallet,
    vipReferralResponse: VipReferralResponse
  ): PromotionsModel {
    var gamificationAvailable = false
    var maxBonus = getMaxBonus(levels)
    val promotions = mutableListOf<Promotion>()
    val perks = mutableListOf<PerkPromotion>()
    userStats.promotions.sortPerks().forEach {
      when (it) {
        is GamificationResponse -> {
          gamificationAvailable = it.status == PromotionsResponse.Status.ACTIVE

          if (levels.isActive) {
            maxBonus = levels.list.maxByOrNull { level -> level.bonus }?.bonus ?: 0.0
          }

          if (gamificationAvailable) {
            promotions.add(0, mapToGamificationItem(it))
          }
        }

        is ReferralResponse -> {
          if (it.status == PromotionsResponse.Status.ACTIVE) {
            val index = if (gamificationAvailable) 1 else 0
            promotions.add(index, mapToReferralItem(it))
          }
        }

        is GenericResponse -> {
          if (isPerk(it.linkedPromotionId)) {
            when {
              isFuturePromotion(it) ->
                perks.add(mapToFutureItem(it))

              it.viewType == PromotionsInteractor.PROGRESS_VIEW_TYPE ->
                perks.add(mapToProgressItem(it))

              it.id == PromotionsInteractor.PROMO_CODE_PERK ->
                perks.add(mapToPromoCodeItem(it))

              else -> perks.add(mapToDefaultItem(it))
            }
          }
          if (isValidGamificationLink(
              promotionsResponse = it,
              gamificationAvailable = gamificationAvailable
            )
          ) {
            mapToGamificationLinkItem(promotions, it)
          }
        }
      }
    }

    val partnerPerk: PartnerPerk? = if (
      userStats.walletOrigin == WalletOrigin.PARTNER ||
      userStats.walletOrigin == WalletOrigin.PARTNER_NO_BONUS
    ) {
      val index = perks.indexOfFirst { it.id == "PARTNER_PERK" }
      if (index != -1) toPartnerPerk(perks.removeAt(index)) else null
    } else
      null

    return PromotionsModel(
      promotions = promotions,
      perks = perks,
      partnerPerk = partnerPerk,
      maxBonus = maxBonus,
      wallet = wallet,
      walletOrigin = map(userStats.walletOrigin),
      error = map(userStats.error),
      fromCache = levels.fromCache && userStats.fromCache,
      vipReferralInfo = vipReferralResponse.map()
    )
  }

  private fun toPartnerPerk(promotion: Promotion): PartnerPerk? {
    val pair = when (promotion) {
      is DefaultItem -> promotion.id to promotion.description
      is FutureItem -> promotion.id to promotion.description
      is ProgressItem -> promotion.id to promotion.description
      is GamificationLinkItem -> promotion.id to promotion.description
      is PromoCodeItem -> promotion.id to promotion.description
      else -> null
    }
    return pair?.let { PartnerPerk(it.first, it.second) }
  }

  private fun map(walletOrigin: WalletOrigin): PromotionsModel.WalletOrigin {
    return when (walletOrigin) {
      WalletOrigin.UNKNOWN -> PromotionsModel.WalletOrigin.UNKNOWN
      WalletOrigin.APTOIDE -> PromotionsModel.WalletOrigin.APTOIDE
      WalletOrigin.PARTNER -> PromotionsModel.WalletOrigin.PARTNER
      WalletOrigin.PARTNER_NO_BONUS -> PromotionsModel.WalletOrigin.PARTNER_NO_BONUS
    }
  }

  private fun map(error: Status?): PromotionsModel.Status? {
    return when (error) {
      null -> null
      Status.NO_NETWORK -> PromotionsModel.Status.NO_NETWORK
      Status.UNKNOWN_ERROR -> PromotionsModel.Status.UNKNOWN_ERROR
    }
  }

  private fun VipReferralResponse.map(): VipReferralInfo? {
    return if (endDate.isNotEmpty() && isAvailable(endDate))
      VipReferralInfo(
        vipBonus = vipBonus,
        vipCode = code,
        totalEarned = earnedUsdAmount,
        totalEarnedConvertedCurrency = earnedCurrencyAmount ?: "",
        numberReferrals = referrals,
        endDate = transformDateToTimestampSeconds(
          date = endDate,
          fromPattern = ISO_8601_DATE_TIME_FORMAT
        ),
        startDate = transformDateToTimestampSeconds(
          date = startDate,
          fromPattern = ISO_8601_DATE_TIME_FORMAT
        ),
        app = app
      )
    else
      null
  }

  private fun isAvailable(endDate: String) =
    transformDateToTimestampSeconds(
      date = endDate,
      fromPattern = ISO_8601_DATE_TIME_FORMAT
    ) * 1000L >= System.currentTimeMillis()

  private fun mapToGamificationLinkItem(
    promotions: MutableList<Promotion>,
    genericResponse: GenericResponse
  ) {
    val gamificationItem = promotions[1] as GamificationItem
    gamificationItem.links.add(
      GamificationLinkItem(
        id = genericResponse.id,
        gamificationStatus = genericResponse.gamificationStatus,
        description = genericResponse.perkDescription,
        icon = genericResponse.icon,
        startDate = genericResponse.startDate,
        endDate = genericResponse.endDate
      )
    )
  }

  private fun mapToProgressItem(genericResponse: GenericResponse): ProgressItem {
    return ProgressItem(
      id = genericResponse.id,
      gamificationStatus = genericResponse.gamificationStatus,
      description = genericResponse.perkDescription,
      appName = genericResponse.icon,
      icon = genericResponse.appName,
      startDate = genericResponse.startDate,
      endDate = genericResponse.endDate,
      current = genericResponse.currentProgress!!,
      objective = genericResponse.objectiveProgress,
      detailsLink = genericResponse.detailsLink
    )
  }

  private fun mapToDefaultItem(genericResponse: GenericResponse): DefaultItem {
    return DefaultItem(
      id = genericResponse.id,
      gamificationStatus = genericResponse.gamificationStatus,
      description = genericResponse.perkDescription,
      icon = genericResponse.icon,
      appName = genericResponse.appName,
      startDate = genericResponse.startDate,
      endDate = genericResponse.endDate,
      detailsLink = genericResponse.detailsLink,
      actionUrl = genericResponse.actionUrl,
      packageName = genericResponse.packageName
    )
  }

  private fun mapToGamificationItem(gamificationResponse: GamificationResponse): GamificationItem {
    val currentLevelInfo = gamificationMapper.mapCurrentLevelInfo(gamificationResponse.level)
    val toNextLevelAmount =
      gamificationResponse.nextLevelAmount?.minus(gamificationResponse.totalSpend)

    return GamificationItem(
      id = gamificationResponse.id,
      planet = currentLevelInfo.planet,
      level = gamificationResponse.level,
      gamificationStatus = gamificationResponse.gamificationStatus,
      levelColor = currentLevelInfo.levelColor,
      title = currentLevelInfo.title,
      toNextLevelAmount = toNextLevelAmount,
      bonus = gamificationResponse.bonus,
      links = mutableListOf()
    )
  }

  private fun mapToReferralItem(referralResponse: ReferralResponse): ReferralItem {
    return ReferralItem(
      id = referralResponse.id,
      bonus = referralResponse.amount,
      currency = referralResponse.currency,
      link = referralResponse.link.orEmpty()
    )
  }

  private fun mapToFutureItem(genericResponse: GenericResponse): FutureItem {
    return FutureItem(
      id = genericResponse.id,
      gamificationStatus = genericResponse.gamificationStatus,
      description = genericResponse.perkDescription,
      icon = genericResponse.icon,
      appName = genericResponse.appName,
      startDate = genericResponse.startDate,
      endDate = genericResponse.endDate,
      detailsLink = genericResponse.detailsLink,
      actionUrl = genericResponse.actionUrl,
      packageName = genericResponse.packageName
    )
  }

  private fun mapToPromoCodeItem(genericResponse: GenericResponse): PromoCodeItem {
    return PromoCodeItem(
      id = genericResponse.id,
      gamificationStatus = genericResponse.gamificationStatus,
      description = genericResponse.perkDescription,
      appName = genericResponse.appName,
      icon = genericResponse.icon,
      startDate = genericResponse.startDate,
      endDate = genericResponse.endDate,
      actionUrl = genericResponse.actionUrl,
      packageName = genericResponse.packageName
    )
  }

  private fun isValidGamificationLink(
    promotionsResponse: GenericResponse,
    gamificationAvailable: Boolean,
  ): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return promotionsResponse.linkedPromotionId == PromotionsInteractor.GAMIFICATION_ID
        && gamificationAvailable
        && (promotionsResponse.startDate ?: 0) < currentTime
  }

  private fun isFuturePromotion(genericResponse: GenericResponse): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return (genericResponse.startDate ?: 0) > currentTime
  }

  private fun getMaxBonus(levels: Levels): Double {
    if (levels.isActive) return levels.list.maxByOrNull { level -> level.bonus }?.bonus ?: 0.0
    return 0.0
  }

  private fun isPerk(linkedPromotionId: String?): Boolean =
    linkedPromotionId != PromotionsInteractor.GAMIFICATION_ID

  // sorting perks by: priority > type > start date/end date
  private fun List<PromotionsResponse>.sortPerks(): List<PromotionsResponse> =
    this.sortedWith { first, second ->
      if (first.priority > second.priority) {
        return@sortedWith -1
      }
      if (first.priority < second.priority) {
        return@sortedWith 1
      }
      if (first is GenericResponse && second is GenericResponse) {
        if (isFuturePromotion(first) && !isFuturePromotion(second)) {
          return@sortedWith 1
        } else if (!isFuturePromotion(first) && isFuturePromotion(second)) {
          return@sortedWith -1
        } else if (isFuturePromotion(first) && isFuturePromotion(second)) {
          if ((first.startDate ?: 0) > (second.startDate ?: 0)) {
            return@sortedWith 1
          }
          if ((first.startDate ?: 0) < (second.startDate ?: 0)) {
            return@sortedWith -1
          }
        } else if (!isFuturePromotion(first) && !isFuturePromotion(second)) {
          if (first.endDate > second.endDate) {
            return@sortedWith 1
          }
          if (first.endDate < second.endDate) {
            return@sortedWith -1
          }
        }
      }
      return@sortedWith 0
    }
}
