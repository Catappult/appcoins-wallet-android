package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.DefaultItem
import com.asfoundation.wallet.promotions.ui.PromotionsViewModel.Companion.DETAILS_URL_EXTRA
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.appcoins.wallet.ui.widgets.SeparatorView
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@EpoxyModelClass
abstract class DefaultModel : EpoxyModelWithHolder<DefaultModel.DefaultHolder>() {

  @EpoxyAttribute
  lateinit var defaultItem: DefaultItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_promotions_default

  override fun bind(holder: DefaultHolder) {

    holder.itemView.isClickable = defaultItem.detailsLink != null

    holder.itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      defaultItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(defaultItem.id, extras))
    }
    holder.activeAppName.text = defaultItem.appName

    holder.activeAppName.visibility = if (defaultItem.appName != null) View.VISIBLE else View.GONE

    holder.activeTitle.text = defaultItem.description
    holder.loadIcon(defaultItem.icon)
    holder.handleExpiryDate(defaultItem.endDate)
    holder.handleVip(defaultItem.gamificationStatus)
    holder.handleSeparator()
  }

  private fun DefaultHolder.loadIcon(icon: String?) {
    GlideApp.with(itemView.context)
      .load(icon)
      .error(R.drawable.ic_promotions_default)
      .circleCrop()
      .into(activeIcon)
  }

  private fun DefaultHolder.handleExpiryDate(endDate: Long) {
    val currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)
    val hours = TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS)
    val minutes = TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS)

    when {
      minutes < 0 -> activeContainerDate.visibility = View.INVISIBLE
      days > 3 -> activeContainerDate.visibility = View.INVISIBLE
      days in 1..3 -> updateDate(days, R.plurals.promotion_ends_short)
      hours > 0 -> updateDate(hours, R.plurals.promotion_ends_hours_short)
      else -> updateDate(minutes, R.plurals.promotion_ends_minutes_short)
    }
  }

  private fun DefaultHolder.updateDate(time: Long, @PluralsRes text: Int) {
    activeContainerDate.visibility = View.VISIBLE
    activeExpiryDate.text =
      itemView.context.resources.getQuantityString(text, time.toInt(), time.toString())
  }

  private fun DefaultHolder.handleVip(gamificationStatus: GamificationStatus?) {
    when (gamificationStatus) {
      GamificationStatus.VIP -> {
        onlyForVip.visibility = View.VISIBLE
        activeIconBorderVip.visibility = View.VISIBLE
        activeIconBorder.visibility = View.GONE
      }
      else -> {
        onlyForVip.visibility = View.GONE
        activeIconBorderVip.visibility = View.GONE
        activeIconBorder.visibility = View.VISIBLE
      }
    }
  }

  private fun DefaultHolder.handleSeparator() {
    if (activeContainerDate.isVisible && onlyForVip.isVisible)
      separator.visibility = View.VISIBLE
    else
      separator.visibility = View.GONE
  }

  class DefaultHolder : BaseViewHolder() {
    val activeIcon by bind<ImageView>(R.id.active_icon)
    val activeIconBorder by bind<ImageView>(R.id.active_icon_border)
    val activeIconBorderVip by bind<ImageView>(R.id.active_icon_border_vip)
    val activeAppName by bind<TextView>(R.id.active_app_name)
    val activeTitle by bind<TextView>(R.id.active_title)
    val activeContainerDate by bind<LinearLayout>(R.id.active_container_date)
    val activeExpiryDate by bind<TextView>(R.id.active_expiry_date)
    val onlyForVip by bind<TextView>(R.id.only_for_vip)
    val separator by bind<SeparatorView>(R.id.separator)
  }
}
