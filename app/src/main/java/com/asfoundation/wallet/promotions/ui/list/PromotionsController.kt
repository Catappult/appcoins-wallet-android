package com.asfoundation.wallet.promotions.ui.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.promotions.ui.list.model.*
import com.asfoundation.wallet.util.CurrencyFormatUtils

class PromotionsController : TypedEpoxyController<PromotionsModel>() {

  private val currencyFormatUtils = CurrencyFormatUtils()

  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun buildModels(data: PromotionsModel) {

    for (promotion in data.promotions) {
      when (promotion) {
        is TitleItem -> add(
            TitleModel_()
                .id("gamification_title_model")
                .titleItem(promotion)
                .currencyFormatUtils(currencyFormatUtils)
        )
        is GamificationItem -> add(
            GamificationModelGroup(promotion, currencyFormatUtils, clickListener)
        )
        is ReferralItem -> add(
            ReferralModel_()
                .id(promotion.id, promotion.link)
                .clickListener(clickListener)
        )
        else -> Unit
      }
    }

    for (perk in data.perks) {
      when (perk) {
        is PromoCodeItem -> add(
            PromoCodeModel_()
                .id(perk.id, perk.detailsLink, perk.startDate.toString(), perk.endDate.toString())
                .promoCodeItem(perk)
                .clickListener(clickListener)
        )
        is DefaultItem -> add(
            DefaultModel_()
                .id(perk.id, perk.detailsLink, perk.startDate.toString(), perk.endDate.toString())
                .defaultItem(perk)
                .clickListener(clickListener)
        )
        is FutureItem -> add(
            FutureModel_()
                .id(perk.id, perk.detailsLink, perk.startDate.toString(), perk.endDate.toString())
                .futureItem(perk)
                .clickListener(clickListener)
        )
        is ProgressItem -> add(
            ProgressModel_()
                .id(perk.id, perk.detailsLink, perk.startDate.toString(), perk.endDate.toString())
        )
        else -> Unit
      }
    }

  }
}