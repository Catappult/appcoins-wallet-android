package com.asfoundation.wallet.promotions.ui.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.promotions.ui.list.model.*

class PromotionsController : TypedEpoxyController<PromotionsModel>() {

  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun buildModels(promotionsModel: PromotionsModel) {

    for (promotion in promotionsModel.promotions) {
      when (promotion) {
        is ReferralItem -> add(
          ReferralModel_()
            .id(promotion.id, promotion.link)
            .clickListener(clickListener)
        )
        else -> Unit
      }
    }

    for (perk in promotionsModel.perks) {
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