package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.ProgressItem
import com.asfoundation.wallet.promotions.ui.PromotionsViewModel.Companion.DETAILS_URL_EXTRA
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.appcoins.wallet.ui.widgets.SeparatorView
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@EpoxyModelClass
abstract class ProgressModel : EpoxyModelWithHolder<ProgressModel.ProgressHolder>() {
  // progress items are not working currently. ProgressItem has not been initialized

  @EpoxyAttribute
  lateinit var progressItem: ProgressItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_promotions_progress

  override fun bind(holder: ProgressHolder) {
    holder.itemView.isClickable = progressItem.detailsLink != null

    holder.itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      progressItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(progressItem.id, extras))
    }

    holder.activeTitle.text = progressItem.description
    holder.loadIcon(progressItem.icon)
    holder.handleExpiryDate(progressItem.endDate)
    holder.activeAppName.text = progressItem.appName
    holder.activeAppName.visibility = if (progressItem.appName != null) View.VISIBLE else View.GONE
    holder.handleVip(progressItem.gamificationStatus)
    holder.handleSeparator()

    if (progressItem.objective != null) {
      holder.setProgressWithObjective(progressItem)
    } else {
      holder.setMaxProgress(progressItem)
    }
  }

  private fun ProgressHolder.setProgressWithObjective(progressItem: ProgressItem) {
    progressCurrent.max = progressItem.objective!!.toInt()
    progressCurrent.progress = progressItem.current.toInt()
    val progress = "${progressItem.current.toInt()}/${progressItem.objective.toInt()}"
    progressLabel.text = progress
  }

  private fun ProgressHolder.setMaxProgress(progressItem: ProgressItem) {
    progressCurrent.max = progressItem.current.toInt()
    progressCurrent.progress = progressItem.current.toInt()
    progressLabel.text = "${progressItem.current.toInt()}"
  }

  private fun ProgressHolder.loadIcon(icon: String?) {
    GlideApp.with(itemView.context)
      .load(icon)
      .error(R.drawable.ic_promotions_default)
      .circleCrop()
      .into(activeIcon)
  }

  private fun ProgressHolder.handleVip(gamificationStatus: GamificationStatus?) {
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

  private fun ProgressHolder.handleSeparator() {
    if (activeContainerDate.isVisible && onlyForVip.isVisible)
      separator.visibility = View.VISIBLE
    else
      separator.visibility = View.GONE
  }

  private fun ProgressHolder.handleExpiryDate(endDate: Long) {
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

  private fun ProgressHolder.updateDate(time: Long, @PluralsRes text: Int) {
    activeContainerDate.visibility = View.VISIBLE
    activeExpiryDate.text =
      itemView.context.resources.getQuantityString(text, time.toInt(), time.toString())
  }

  class ProgressHolder : BaseViewHolder() {
    val activeIcon by bind<ImageView>(R.id.active_icon)
    val activeIconBorder by bind<ImageView>(R.id.active_icon_border)
    val activeIconBorderVip by bind<ImageView>(R.id.active_icon_border_vip)
    val activeTitle by bind<TextView>(R.id.active_title)
    val activeAppName by bind<TextView>(R.id.active_app_name)
    val activeContainerDate by bind<LinearLayout>(R.id.active_container_date)
    val activeExpiryDate by bind<TextView>(R.id.active_expiry_date)
    val progressCurrent by bind<ProgressBar>(R.id.progress_current)
    val progressLabel by bind<TextView>(R.id.progress_label)
    val onlyForVip by bind<TextView>(R.id.only_for_vip)
    val separator by bind<SeparatorView>(R.id.separator)
  }
}