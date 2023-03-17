package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.FutureItem
import com.asfoundation.wallet.promotions.ui.PromotionsViewModel.Companion.DETAILS_URL_EXTRA
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.common.SeparatorView

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
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(futureItem.id, extras))
    }

    holder.futureAppName.text = futureItem.appName
    holder.loadIcon(futureItem.icon)

    holder.futureTitle.text = futureItem.description
    holder.handleVip(futureItem.gamificationStatus)
    holder.handleSeparator()
  }

  private fun FutureHolder.loadIcon(icon: String?) {
    GlideApp.with(itemView.context)
      .load(icon)
      .error(R.drawable.ic_promotions_default)
      .circleCrop()
      .into(futureIcon)
  }

  private fun FutureHolder.handleVip(gamificationStatus: GamificationStatus?) {
    when (gamificationStatus) {
      GamificationStatus.VIP -> {
        onlyForVip.visibility = View.VISIBLE
        futureIconBorderVip.visibility = View.VISIBLE
        futureIconBorder.visibility = View.GONE
      }
      else -> {
        onlyForVip.visibility = View.GONE
        futureIconBorderVip.visibility = View.GONE
        futureIconBorder.visibility = View.VISIBLE
      }
    }
  }

  private fun FutureHolder.handleSeparator() {
    if (futureContainerDate.isVisible && onlyForVip.isVisible)
      separator.visibility = View.VISIBLE
    else
      separator.visibility = View.GONE
  }

  class FutureHolder : BaseViewHolder() {
    val futureTitle by bind<TextView>(R.id.future_title)
    val futureAppName by bind<TextView>(R.id.future_app_name)
    val futureIcon by bind<ImageView>(R.id.future_icon)
    val futureIconBorder by bind<ImageView>(R.id.future_icon_border)
    val futureIconBorderVip by bind<ImageView>(R.id.future_icon_border_vip)
    val futureContainerDate by bind<LinearLayout>(R.id.future_container_date)
    val onlyForVip by bind<TextView>(R.id.only_for_vip)
    val separator by bind<SeparatorView>(R.id.separator)
  }
}