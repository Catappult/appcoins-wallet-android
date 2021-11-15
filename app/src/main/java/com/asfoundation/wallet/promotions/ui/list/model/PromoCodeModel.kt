package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.PluralsRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.PromoCodeItem
import com.asfoundation.wallet.promotions.ui.PromotionsViewModel.Companion.DETAILS_URL_EXTRA
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import java.util.concurrent.TimeUnit

@EpoxyModelClass
abstract class PromoCodeModel : EpoxyModelWithHolder<PromoCodeModel.PromoCodeHolder>() {

  @EpoxyAttribute
  lateinit var promoCodeItem: PromoCodeItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_promotions_promo_code

  override fun bind(holder: PromoCodeHolder) {

    holder.itemView.isClickable = promoCodeItem.detailsLink != null

    holder.itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      promoCodeItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(promoCodeItem.id, extras))
    }
    holder.activeAppName.text = promoCodeItem.appName
    holder.activeAppName.visibility = if (promoCodeItem.appName != null) View.VISIBLE else View.GONE

    holder.activeTitle.text = promoCodeItem.description
    holder.loadIcon(promoCodeItem.icon)
    holder.handleExpiryDate(promoCodeItem.endDate)
  }

  private fun PromoCodeHolder.loadIcon(icon: String?) {
    GlideApp.with(itemView.context)
        .load(icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(activeIcon)
  }

  protected fun PromoCodeHolder.handleExpiryDate(endDate: Long) {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)
    val hours = TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS)
    val minutes = TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS)

    when {
      days > 3 -> activeContainerDate.visibility = View.INVISIBLE
      days in 1..3 -> updateDate(days, R.plurals.promotion_ends_short)
      hours > 0 -> updateDate(hours, R.plurals.promotion_ends_hours_short)
      else -> updateDate(minutes, R.plurals.promotion_ends_minutes_short)
    }
  }

  private fun PromoCodeHolder.updateDate(time: Long, @PluralsRes text: Int) {
    activeContainerDate.visibility = View.VISIBLE
    activeExpiryDate.text =
        itemView.context.resources.getQuantityString(text, time.toInt(), time.toString())
  }


  class PromoCodeHolder : BaseViewHolder() {
    val activeIcon by bind<ImageView>(R.id.active_icon)
    val activeAppName by bind<TextView>(R.id.active_app_name)
    val activeTitle by bind<TextView>(R.id.active_title)
    val activeContainerDate by bind<LinearLayout>(R.id.active_container_date)
    val activeExpiryDate by bind<TextView>(R.id.active_expiry_date)
  }
}


