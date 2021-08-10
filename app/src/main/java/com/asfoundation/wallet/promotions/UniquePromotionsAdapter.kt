package com.asfoundation.wallet.promotions

import DefaultViewHolder
import GamificationViewHolder
import PromotionsViewHolder
import ReferralViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.Promotion
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.ui.list.PromotionsAdapter
import io.reactivex.subjects.PublishSubject

class UniquePromotionsAdapter(uniquePromotions: List<Promotion>,
                              private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsAdapter() {

  init {
    currentList = uniquePromotions
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionsViewHolder {
    return when (viewType) {
      GAMIFICATION_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_gamification, parent, false)
        GamificationViewHolder(layout, clickListener)
      }
      REFERRAL_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_referrals, parent, false)
        ReferralViewHolder(layout, clickListener)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_default, parent, false)
        DefaultViewHolder(layout, clickListener)
      }
    }
  }
}