package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.core.network.backend.model.GamificationResponse
import com.appcoins.wallet.core.network.backend.model.GenericResponse
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.ISO_8601_DATE_TIME_FORMAT
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.MONTH_DAY_YEAR_FORMAT
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.transformDate
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
      vouchersListModel: VoucherListModel,
      vipReferralResponse: VipReferralResponse
  ): PromotionsModel {
    var gamificationAvailable = false
    var referralAvailable = false
    var maxBonus = getMaxBonus(levels)
    val promotions = mutableListOf<Promotion>()
    val perks = mutableListOf<PerkPromotion>()
    val vouchers = handleVouchers(vouchersListModel, maxBonus)
    userStats.promotions.sortPerks()
      .forEach {
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
            referralAvailable = it.status == PromotionsResponse.Status.ACTIVE
            if (referralAvailable) {
              val index = if (gamificationAvailable) 1 else 0
              promotions.add(index, mapToReferralItem(it))
            }
          }
          is GenericResponse -> {
            if (isPerk(it.linkedPromotionId)) {
              when {
                isFuturePromotion(it) -> perks.add(mapToFutureItem(it))
                it.viewType == PromotionsInteractor.PROGRESS_VIEW_TYPE -> perks.add(
                  mapToProgressItem(it)
                )
                it.id == PromotionsInteractor.PROMO_CODE_PERK -> perks.add(
                  mapToPromoCodeItem(
                    it
                  )
                )
                else -> perks.add(mapToDefaultItem(it))
              }
            }
            if (isValidGamificationLink(
                it.linkedPromotionId, gamificationAvailable,
                it.startDate ?: 0
              )
            ) {
              mapToGamificationLinkItem(promotions, it)
            }
          }
        }
      }

    return PromotionsModel(
      promotions, vouchers, perks, maxBonus, wallet,
      map(userStats.walletOrigin),
      map(userStats.error),
      levels.fromCache && userStats.fromCache,
      vipReferralResponse.map()
    )
  }

  private fun map(
    walletOrigin: WalletOrigin
  ): PromotionsModel.WalletOrigin {
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

  private fun VipReferralResponse.map() =
    if (active) VipReferralInfo(
      vipBonus,
      code,
      earnedUsdAmount,
      referrals,
      transformDate(
        date = endDate,
        fromPattern = ISO_8601_DATE_TIME_FORMAT,
        toPattern = MONTH_DAY_YEAR_FORMAT
      )
    )
    else null

  private fun mapToGamificationLinkItem(
    promotions: MutableList<Promotion>,
    genericResponse: GenericResponse
  ) {
    val gamificationItem = promotions[1] as GamificationItem
    gamificationItem.links.add(
      GamificationLinkItem(
        genericResponse.id,
        genericResponse.gamificationStatus,
        genericResponse.perkDescription,
        genericResponse.icon,
        genericResponse.startDate,
        genericResponse.endDate
      )
    )
  }

  private fun mapToProgressItem(genericResponse: GenericResponse): ProgressItem {
    return ProgressItem(
      genericResponse.id,
      genericResponse.gamificationStatus,
      genericResponse.perkDescription,
      genericResponse.icon,
      genericResponse.appName,
      genericResponse.startDate,
      genericResponse.endDate,
      genericResponse.currentProgress!!,
      genericResponse.objectiveProgress,
      genericResponse.detailsLink
    )
  }

  private fun mapToDefaultItem(genericResponse: GenericResponse): DefaultItem {
    return DefaultItem(
      genericResponse.id,
      genericResponse.gamificationStatus,
      genericResponse.perkDescription,
      genericResponse.icon,
      genericResponse.appName,
      genericResponse.startDate,
      genericResponse.endDate,
      genericResponse.detailsLink,
      genericResponse.actionUrl,
      genericResponse.packageName
    )
  }

  private fun mapToGamificationItem(
    gamificationResponse: GamificationResponse
  ): GamificationItem {
    val currentLevelInfo = gamificationMapper.mapCurrentLevelInfo(gamificationResponse.level)
    val toNextLevelAmount =
      gamificationResponse.nextLevelAmount?.minus(gamificationResponse.totalSpend)

    return GamificationItem(
      gamificationResponse.id,
      currentLevelInfo.planet,
      gamificationResponse.level,
      gamificationResponse.gamificationStatus,
      currentLevelInfo.levelColor,
      currentLevelInfo.title,
      toNextLevelAmount,
      gamificationResponse.bonus,
      mutableListOf()
    )
  }

  private fun mapToReferralItem(referralResponse: ReferralResponse): ReferralItem {
    return ReferralItem(
      referralResponse.id, referralResponse.amount, referralResponse.currency,
      referralResponse.link.orEmpty()
    )
  }

  private fun mapToFutureItem(genericResponse: GenericResponse): FutureItem {
    return FutureItem(
      genericResponse.id,
      genericResponse.gamificationStatus,
      genericResponse.perkDescription,
      genericResponse.icon,
      genericResponse.appName,
      genericResponse.startDate,
      genericResponse.endDate,
      genericResponse.detailsLink,
      genericResponse.actionUrl,
      genericResponse.packageName
    )
  }

  private fun mapToPromoCodeItem(genericResponse: GenericResponse): PromoCodeItem {
    return PromoCodeItem(
      genericResponse.id,
      genericResponse.gamificationStatus,
      genericResponse.perkDescription,
      genericResponse.appName,
      genericResponse.icon,
      genericResponse.startDate,
      genericResponse.endDate,
      genericResponse.actionUrl,
      genericResponse.packageName
    )
  }

  private fun isValidGamificationLink(
    linkedPromotionId: String?,
    gamificationAvailable: Boolean, startDate: Long
  ): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return linkedPromotionId != null && linkedPromotionId == PromotionsInteractor.GAMIFICATION_ID && gamificationAvailable && startDate < currentTime
  }

  private fun isFuturePromotion(genericResponse: GenericResponse): Boolean {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    return genericResponse.startDate ?: 0 > currentTime
  }

  private fun getMaxBonus(levels: Levels): Double {
    if (levels.isActive) return levels.list.maxByOrNull { level -> level.bonus }?.bonus ?: 0.0
    return 0.0
  }

  private fun handleVouchers(
    vouchersListModel: VoucherListModel,
    maxBonus: Double
  ): List<VoucherItem> {
    val list = ArrayList<VoucherItem>()
    vouchersListModel.vouchers.forEach {
      list.add(
        VoucherItem(
          PromotionsInteractor.VOUCHER_ID, it.packageName, it.title, it.icon,
          it.hasAppcoins, maxBonus
        )
      )
    }
    return list
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
          if (first.startDate ?: 0 > second.startDate ?: 0) {
            return@sortedWith 1
          }
          if (first.startDate ?: 0 < second.startDate ?: 0) {
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