package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.GamificationItem
import com.asfoundation.wallet.promotions.model.GamificationLinkItem
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import java.text.DecimalFormat

class GamificationModelGroup(private val gamificationItem: GamificationItem,
                             private val currencyFormatUtils: CurrencyFormatUtils,
                             private val clickListener: ((PromotionClick) -> Unit)? = null) :
    EpoxyModelGroup(R.layout.item_promotions_gamification, buildModels(gamificationItem.links)) {

  override fun bind(holder: ModelGroupHolder) {
    super.bind(holder)
    val context = holder.rootView.context
    val gamificationMapper = GamificationMapper(context, currencyFormatUtils)
    val planet = holder.rootView.findViewById<ImageView>(R.id.planet)
    val currentLevelBonus = holder.rootView.findViewById<TextView>(R.id.current_level_bonus)
    val planetTitle = holder.rootView.findViewById<TextView>(R.id.planet_title)
    val planetSubtitle = holder.rootView.findViewById<TextView>(R.id.planet_subtitle)

    val df = DecimalFormat("###.#")

    holder.rootView.setOnClickListener {
      clickListener?.invoke(PromotionClick(gamificationItem.id))
    }
    planet.setImageDrawable(gamificationItem.planet)
    currentLevelBonus.background = gamificationMapper.getOvalBackground(gamificationItem.levelColor)
    currentLevelBonus.text =
        context.getString(R.string.gamif_bonus, df.format(gamificationItem.bonus))
    planetTitle.text = gamificationItem.title
    if (gamificationItem.toNextLevelAmount != null) {
      planetSubtitle.text = context.getString(R.string.gamif_card_body,
          currencyFormatUtils.formatGamificationValues(gamificationItem.toNextLevelAmount))
    } else {
      planetSubtitle.visibility = View.INVISIBLE
    }
  }

  companion object {
    fun buildModels(links: List<GamificationLinkItem>): List<EpoxyModel<*>> {
      if (links.isEmpty()) return listOf(
          EmptyViewModel_()
              .id("gamification_no_links")
      )
      return links.map { link ->
        GamificationLinkModel_()
            .id("gamification_link_model", link.id, link.detailsLink, link.endDate.toString())
            .gamificationLinkItem(link)
      }
    }

    class GamificationHolder : BaseViewHolder() {
      val title by bind<TextView>(R.id.title)
    }
  }
}