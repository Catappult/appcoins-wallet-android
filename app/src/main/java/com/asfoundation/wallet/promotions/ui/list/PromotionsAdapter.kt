package com.asfoundation.wallet.promotions.ui.list

import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.promotions.PromotionsViewHolder
import com.asfoundation.wallet.promotions.model.*

abstract class PromotionsAdapter : RecyclerView.Adapter<PromotionsViewHolder>() {

  internal var currentList: List<Promotion> = emptyList()

  companion object {
    internal const val TITLE_VIEW_TYPE = 0
    internal const val GAMIFICATION_VIEW_TYPE = 1
    internal const val PROGRESS_VIEW_TYPE = 2
    internal const val DEFAULT_VIEW_TYPE = 3
    internal const val FUTURE_VIEW_TYPE = 4
    internal const val REFERRAL_VIEW_TYPE = 5
    internal const val VOUCHERS_VIEW_TYPE = 6
  }

  override fun getItemCount() = currentList.size

  override fun onBindViewHolder(holder: PromotionsViewHolder, position: Int) {
    holder.bind(currentList[position])
  }

  override fun getItemViewType(position: Int): Int {
    return when (currentList[position]) {
      is GamificationItem -> GAMIFICATION_VIEW_TYPE
      is ReferralItem -> REFERRAL_VIEW_TYPE
      is ProgressItem -> PROGRESS_VIEW_TYPE
      is FutureItem -> FUTURE_VIEW_TYPE
      is VoucherItem -> VOUCHERS_VIEW_TYPE
      is TitleItem -> TITLE_VIEW_TYPE
      else -> DEFAULT_VIEW_TYPE
    }
  }

  fun setPromotions(promotionsList: List<Promotion>) {
    if (!areTheSame(currentList, promotionsList)) {
      currentList = promotionsList
      notifyDataSetChanged()
    }
  }

  private fun areTheSame(currentList: List<Promotion>, promotionsList: List<Promotion>): Boolean {
    if (currentList.size != promotionsList.size) {
      return false
    }
    return currentList.zip(promotionsList)
        .all { (x, y) -> x.id == y.id }
  }
}