package com.asfoundation.wallet.promotions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import kotlinx.android.synthetic.main.item_gamification_link.view.*


class PromotionsGamificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(gamificationLinkItem: GamificationLinkItem) {
    GlideApp.with(itemView.context)
        .load(gamificationLinkItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.link_icon)

    itemView.link_description.text = gamificationLinkItem.description
  }
}