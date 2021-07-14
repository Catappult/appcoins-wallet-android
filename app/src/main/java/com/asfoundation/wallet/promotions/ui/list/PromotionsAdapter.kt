package com.asfoundation.wallet.promotions.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.*
import com.asfoundation.wallet.promotions.model.*


class PromotionsAdapter(private var promotions: List<Promotion>,
                        private val clickListener: (PromotionClick) -> Unit) :
    RecyclerView.Adapter<PromotionsViewHolder>() {

  companion object {
    private const val TITLE_VIEW_TYPE = 0
    private const val GAMIFICATION_VIEW_TYPE = 1
    private const val PROGRESS_VIEW_TYPE = 2
    private const val DEFAULT_VIEW_TYPE = 3
    private const val FUTURE_VIEW_TYPE = 4
    private const val REFERRAL_VIEW_TYPE = 5
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionsViewHolder {
    return when (viewType) {
      GAMIFICATION_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_gamification, parent, false)
        GamificationViewHolder(layout, clickListener)
      }
      PROGRESS_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_progress, parent, false)
        ProgressViewHolder(layout, clickListener)
      }
      TITLE_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_title, parent, false)
        TitleViewHolder(layout)
      }
      FUTURE_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotions_future, parent, false)
        FutureViewHolder(layout, clickListener)
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

  override fun getItemCount() = promotions.size

  override fun getItemViewType(position: Int): Int {
    return when (promotions[position]) {
      is GamificationItem -> GAMIFICATION_VIEW_TYPE
      is TitleItem -> TITLE_VIEW_TYPE
      is ProgressItem -> PROGRESS_VIEW_TYPE
      is FutureItem -> FUTURE_VIEW_TYPE
      is ReferralItem -> REFERRAL_VIEW_TYPE
      else -> DEFAULT_VIEW_TYPE
    }
  }

  override fun onBindViewHolder(holder: PromotionsViewHolder, position: Int) {
    holder.bind(promotions[position])
  }

  fun setPromotions(promotionsList: List<Promotion>) {
    if (!areTheSame(promotions, promotionsList)) {
      promotions = promotionsList
      notifyDataSetChanged()
    }
  }

  private fun areTheSame(currentList: List<Promotion>, promotionsList: List<Promotion>): Boolean {
    if (currentList.size != promotionsList.size) {
      return false
    }
    return promotions.zip(promotionsList)
        .all { (x, y) -> x.id == y.id }
  }
}