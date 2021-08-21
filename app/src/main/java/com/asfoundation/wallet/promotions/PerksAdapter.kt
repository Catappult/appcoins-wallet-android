package com.asfoundation.wallet.promotions

import android.view.LayoutInflater
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.PerkPromotion
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.ui.list.PromotionsAdapter
import io.reactivex.subjects.PublishSubject


class PerksAdapter(perks: List<PerkPromotion>,
                   private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsAdapter() {

  init {
    currentList = perks
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionsViewHolder {
    return when (viewType) {
      PROGRESS_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_progress, parent, false)
        ProgressViewHolder(layout, clickListener)
      }
      FUTURE_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_future, parent, false)
        FutureViewHolder(layout, clickListener)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_default, parent, false)
        DefaultViewHolder(layout, clickListener)
      }
    }
  }
}