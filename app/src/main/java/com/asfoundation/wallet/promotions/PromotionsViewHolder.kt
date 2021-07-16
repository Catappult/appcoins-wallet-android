package com.asfoundation.wallet.promotions

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import kotlinx.android.synthetic.main.item_promotions_default.view.*
import kotlinx.android.synthetic.main.item_promotions_future.view.*
import kotlinx.android.synthetic.main.item_promotions_gamification.view.*
import kotlinx.android.synthetic.main.item_promotions_progress.view.*
import kotlinx.android.synthetic.main.item_promotions_referrals.view.*
import kotlinx.android.synthetic.main.item_promotions_title.view.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

abstract class PromotionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  companion object {
    const val DETAILS_URL_EXTRA = "DETAILS_URL_EXTRA"
  }

  abstract fun bind(promotion: Promotion)

  protected fun handleExpiryDate(view: TextView, container: LinearLayout, endDate: Long) {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)
    val hours = TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS)
    val minutes = TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS)

    when {
      days > 3 -> container.visibility = View.GONE
      days in 1..3 -> updateDate(view, container, days, R.plurals.promotion_ends)
      hours > 0 -> updateDate(view, container, hours, R.plurals.promotion_ends_hours)
      else -> updateDate(view, container, minutes, R.plurals.promotion_ends_minutes)
    }
  }

  private fun updateDate(view: TextView, container: LinearLayout, time: Long,
                         @PluralsRes text: Int) {
    container.visibility = View.VISIBLE
    view.text =
        itemView.context.resources.getQuantityString(text, time.toInt(), time.toString())
  }

}

class TitleViewHolder(itemView: View) : PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val titleItem = promotion as TitleItem

    val title = if (titleItem.isGamificationTitle) {
      val formatter = CurrencyFormatUtils.create()
      val bonus = formatter.formatGamificationValues(BigDecimal(titleItem.bonus))
      itemView.context.getString(titleItem.title, bonus)
    } else itemView.context.getString(titleItem.title)
    itemView.promotions_title.text = title
    itemView.promotions_subtitle.setText(titleItem.subtitle)
  }

}

class ProgressViewHolder(itemView: View,
                         private val clickListener: (PromotionClick) -> Unit) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val progressItem = promotion as ProgressItem

    itemView.isClickable = false

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      progressItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener(PromotionClick(promotion.id, extras))
    }

    GlideApp.with(itemView.context)
        .load(progressItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.progress_icon)

    itemView.progress_title.text = progressItem.description
    if (progressItem.objective != null) {
      itemView.progress_current.max = progressItem.objective.toInt()
      itemView.progress_current.progress = progressItem.current.toInt()
      val progress = "${progressItem.current.toInt()}/${progressItem.objective.toInt()}"
      itemView.progress_label.text = progress
    } else {
      itemView.progress_current.max = progressItem.current.toInt()
      itemView.progress_current.progress = progressItem.current.toInt()
      itemView.progress_label.text = "${progressItem.current.toInt()}"
    }
    handleExpiryDate(itemView.progress_expiry_date, itemView.progress_container_date,
        progressItem.endDate)
  }

}

class DefaultViewHolder(itemView: View,
                        private val clickListener: (PromotionClick) -> Unit) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val defaultItem = promotion as DefaultItem

    itemView.isClickable = false

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      defaultItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener(PromotionClick(promotion.id, extras))
    }

    GlideApp.with(itemView.context)
        .load(defaultItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.default_icon)

    itemView.default_title.text = defaultItem.description
    handleExpiryDate(itemView.default_expiry_date, itemView.default_container_date,
        defaultItem.endDate)
  }

}

class FutureViewHolder(itemView: View,
                       private val clickListener: (PromotionClick) -> Unit) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val futureItem = promotion as FutureItem

    itemView.isClickable = false

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      futureItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener(PromotionClick(promotion.id, extras))
    }

    GlideApp.with(itemView.context)
        .load(futureItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.future_icon)

    itemView.future_title.text = futureItem.description
  }

}

class ReferralViewHolder(itemView: View,
                         private val clickListener: (PromotionClick) -> Unit) :
    PromotionsViewHolder(itemView) {

  companion object {
    const val KEY_ACTION = "ACTION"
    const val KEY_LINK = "LINK"
    const val ACTION_DETAILS = "DETAILS"
    const val ACTION_SHARE = "SHARE"
  }

  override fun bind(promotion: Promotion) {
    val referralItem = promotion as ReferralItem

    itemView.setOnClickListener {
      val extras = mapOf(
          Pair(KEY_LINK, referralItem.link),
          Pair(KEY_ACTION, ACTION_DETAILS)
      )
      clickListener(PromotionClick(promotion.id, extras))
    }

    itemView.share_container.setOnClickListener {
      val extras = mapOf(
          Pair(KEY_LINK, referralItem.link),
          Pair(KEY_ACTION, ACTION_SHARE)
      )
      clickListener(PromotionClick(promotion.id, extras))
    }

    val formatter = CurrencyFormatUtils.create()
    val bonus = formatter.formatCurrency(referralItem.bonus, WalletCurrency.FIAT)

    val subtitle = itemView.context.getString(R.string.promotions_referral_card_title,
        referralItem.currency + bonus)

    itemView.referral_subtitle.text = subtitle
  }

}

class GamificationViewHolder(itemView: View,
                             private val clickListener: (PromotionClick) -> Unit) :
    PromotionsViewHolder(itemView) {

  init {
    itemView.linked_perks.addItemDecoration(
        MarginItemDecoration(itemView.resources.getDimension(R.dimen.promotions_item_margin)
            .toInt()))
  }

  private var mapper = GamificationMapper(itemView.context)

  override fun bind(promotion: Promotion) {
    val gamificationItem = promotion as GamificationItem
    val formatter = CurrencyFormatUtils.create()
    val df = DecimalFormat("###.#")

    itemView.setOnClickListener {
      clickListener(PromotionClick(promotion.id))
    }

    itemView.planet.setImageDrawable(gamificationItem.planet)
    itemView.current_level_bonus.background = mapper.getOvalBackground(gamificationItem.levelColor)
    itemView.current_level_bonus.text =
        itemView.context?.getString(R.string.gamif_bonus, df.format(gamificationItem.bonus))
    itemView.planet_title.text = gamificationItem.title
    if (gamificationItem.toNextLevelAmount != null) {
      itemView.planet_subtitle.text = itemView.context.getString(R.string.gamif_card_body,
          formatter.formatGamificationValues(gamificationItem.toNextLevelAmount))
    } else {
      itemView.planet_subtitle.visibility = View.INVISIBLE
    }

    handleLinks(gamificationItem.links, itemView)
  }

  private fun handleLinks(links: List<GamificationLinkItem>, itemView: View) {
    if (links.isEmpty()) {
      itemView.linked_perks.visibility = View.GONE
    } else {
      itemView.linked_perks.visibility = View.VISIBLE
      val adapter = PromotionsGamificationAdapter(links)
      itemView.linked_perks.adapter = adapter
    }
  }
}