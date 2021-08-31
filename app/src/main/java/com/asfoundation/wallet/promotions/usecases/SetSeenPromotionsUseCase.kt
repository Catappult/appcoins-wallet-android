package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.model.GamificationItem
import com.asfoundation.wallet.promotions.model.PerkPromotion
import com.asfoundation.wallet.promotions.model.Promotion

class SetSeenPromotionsUseCase(val promotionsRepository: PromotionsRepository) {

  operator fun invoke(promotions: List<Promotion>, wallet: String) {
    promotions.forEach {
      when (it) {
        is GamificationItem -> {
          promotionsRepository.shownLevel(wallet, it.level, GamificationContext.SCREEN_PROMOTIONS)
          it.links.forEach { gamificationLinkItem ->
            promotionsRepository.setSeenGenericPromotion(
                getPromotionIdKey(gamificationLinkItem.id, gamificationLinkItem.startDate,
                    gamificationLinkItem.endDate), PromotionUpdateScreen.PROMOTIONS.name)
          }
        }
        is PerkPromotion -> promotionsRepository.setSeenGenericPromotion(
            getPromotionIdKey(it.id, it.startDate, it.endDate),
            PromotionUpdateScreen.PROMOTIONS.name)
        else -> Unit
      }
    }
  }

  private fun getPromotionIdKey(id: String, startDate: Long?, endDate: Long): String {
    return id + "_" + startDate + "_" + endDate
  }
}