package com.asfoundation.wallet.promotions.ui.list.model

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.PromotionsViewHolder
import com.asfoundation.wallet.promotions.model.FutureItem
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.ui.common.BaseViewHolder

@EpoxyModelClass
abstract class FutureModel : EpoxyModelWithHolder<FutureModel.FutureHolder>() {

  @EpoxyAttribute
  lateinit var futureItem: FutureItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_promotions_future

  override fun bind(holder: FutureHolder) {
    holder.itemView.isClickable = futureItem.detailsLink != null

    holder.itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      futureItem.detailsLink?.let {
        extras[PromotionsViewHolder.DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(futureItem.id, extras))
    }

    holder.futureAppName.text = futureItem.appName
    holder.loadIcon(futureItem.icon)

    holder.futureTitle.text = futureItem.description
  }

  private fun FutureHolder.loadIcon(icon: String?) {
    GlideApp.with(itemView.context)
        .load(icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(futureIcon)
  }

  class FutureHolder : BaseViewHolder() {
    val futureTitle by bind<TextView>(R.id.future_title)
    val futureAppName by bind<TextView>(R.id.future_app_name)
    val futureIcon by bind<ImageView>(R.id.future_icon)
  }
}