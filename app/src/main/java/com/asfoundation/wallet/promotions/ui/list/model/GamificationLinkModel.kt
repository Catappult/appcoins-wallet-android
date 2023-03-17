package com.asfoundation.wallet.promotions.ui.list.model

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.GamificationLinkItem
import com.appcoins.wallet.ui.widgets.BaseViewHolder

@EpoxyModelClass
abstract class GamificationLinkModel :
    EpoxyModelWithHolder<GamificationLinkModel.GamificationLinkHolder>() {

  @EpoxyAttribute
  lateinit var gamificationLinkItem: GamificationLinkItem

  override fun bind(holder: GamificationLinkHolder) {
    GlideApp.with(holder.itemView.context)
        .load(gamificationLinkItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(holder.linkIcon)

    holder.linkDescription.text = gamificationLinkItem.description
  }

  override fun getDefaultLayout(): Int = R.layout.item_gamification_link

  class GamificationLinkHolder : BaseViewHolder() {
    val linkDescription by bind<TextView>(R.id.link_description)
    val linkIcon by bind<ImageView>(R.id.link_icon)
  }
}