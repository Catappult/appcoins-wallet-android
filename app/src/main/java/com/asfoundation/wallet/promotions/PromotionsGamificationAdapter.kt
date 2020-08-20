package com.asfoundation.wallet.promotions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import kotlinx.android.synthetic.main.item_gamification_link.view.*


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

class PromotionsGamificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(gamificationLinkItem: GamificationLinkItem) {
    GlideApp.with(itemView.context)
        .load(gamificationLinkItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.link_icon)

    itemView.link_description.text = gamificationLinkItem.title
  }

}


