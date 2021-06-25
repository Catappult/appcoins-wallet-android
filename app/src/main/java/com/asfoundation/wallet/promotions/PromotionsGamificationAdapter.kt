package com.asfoundation.wallet.promotions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.GamificationLinkItem


class PromotionsGamificationAdapter(private val links: List<GamificationLinkItem>) :
    RecyclerView.Adapter<PromotionsGamificationViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): PromotionsGamificationViewHolder {

    val layout = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_gamification_link, parent, false)
    return PromotionsGamificationViewHolder(layout)
  }

  override fun getItemCount() = links.size

  override fun onBindViewHolder(holder: PromotionsGamificationViewHolder, position: Int) {
    holder.bind(links[position])
  }

}


