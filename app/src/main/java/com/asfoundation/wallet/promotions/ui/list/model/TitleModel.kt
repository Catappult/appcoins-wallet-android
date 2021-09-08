package com.asfoundation.wallet.promotions.ui.list.model

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.TitleItem
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import java.math.BigDecimal

@EpoxyModelClass
abstract class TitleModel : EpoxyModelWithHolder<TitleModel.TitleHolder>() {

  @EpoxyAttribute
  lateinit var titleItem: TitleItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  override fun bind(holder: TitleHolder) {
    val context = holder.itemView.context

    val title = if (titleItem.isGamificationTitle) {
      val bonus = currencyFormatUtils.formatGamificationValues(BigDecimal(titleItem.bonus))
      context.getString(titleItem.title, bonus)
    } else {
      context.getString(titleItem.title)
    }
    holder.title.text = title
    holder.subtitle.setText(titleItem.subtitle)
  }

  override fun getDefaultLayout(): Int = R.layout.item_promotions_title

  class TitleHolder : BaseViewHolder() {
    val title by bind<TextView>(R.id.promotions_title)
    val subtitle by bind<TextView>(R.id.promotions_subtitle)
  }
}