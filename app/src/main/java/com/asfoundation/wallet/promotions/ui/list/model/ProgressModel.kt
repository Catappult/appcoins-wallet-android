package com.asfoundation.wallet.promotions.ui.list.model

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.PluralsRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.PromotionsViewHolder
import com.asfoundation.wallet.promotions.model.ProgressItem
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import java.util.concurrent.TimeUnit

@EpoxyModelClass
abstract class ProgressModel : EpoxyModelWithHolder<ProgressModel.ProgressHolder>() {

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
        extras[PromotionsViewHolder.DETAILS_URL_EXTRA] = it
      }
      clickListener?.invoke(PromotionClick(progressItem.id, extras))
    }

    holder.activeTitle.text = progressItem.description
    holder.loadIcon(progressItem.icon)
    holder.handleExpiryDate(progressItem.endDate)
    holder.activeAppName.text = progressItem.appName

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

  protected fun ProgressHolder.handleExpiryDate(endDate: Long) {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)
    val hours = TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS)
    val minutes = TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS)

    when {
      days > 3 -> activeContainerDate.visibility = View.GONE
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
    val activeTitle by bind<TextView>(R.id.active_title)
    val activeAppName by bind<TextView>(R.id.active_app_name)
    val activeContainerDate by bind<LinearLayout>(R.id.active_container_date)
    val activeExpiryDate by bind<TextView>(R.id.active_expiry_date)
    val progressCurrent by bind<ProgressBar>(R.id.progress_current)
    val progressLabel by bind<TextView>(R.id.progress_label)
  }
}