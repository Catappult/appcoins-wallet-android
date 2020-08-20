package com.asfoundation.wallet.promotions

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_promotions_default.view.*
import kotlinx.android.synthetic.main.item_promotions_gamification.view.*
import kotlinx.android.synthetic.main.item_promotions_progress.view.*
import kotlinx.android.synthetic.main.item_promotions_title.view.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

abstract class PromotionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  abstract fun bind(promotions: Promotion)

  protected fun handleExpiryDate(textView: TextView, containerDate: LinearLayout, endDate: Long) {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)

    if (days > 3) {
      containerDate.visibility = View.GONE
    } else {
      containerDate.visibility = View.VISIBLE
      textView.text = itemView.context.getString(R.string.perks_end_in_days, days.toString())
    }
  }

}

class TitleViewHolder(itemView: View) : PromotionsViewHolder(itemView) {

  override fun bind(promotions: Promotion) {
    val titleItem = promotions as TitleItem

    val title = if (titleItem.isGamificationTitle) {
      itemView.context.getString(titleItem.title, titleItem.bonus)
    } else itemView.context.getString(titleItem.title)
    itemView.promotions_title.text = title
    itemView.promotions_subtitle.setText(titleItem.subtitle)
  }

}

class ProgressViewHolder(itemView: View,
                         private val clickListener: PublishSubject<String>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotions: Promotion) {
    val progressItem = promotions as ProgressItem

    itemView.setOnClickListener {
      clickListener.onNext(promotions.id)
    }

    GlideApp.with(itemView.context)
        .load(progressItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.progress_icon)

    itemView.progress_title.text = progressItem.title
    itemView.progress_current.progress = progressItem.current.toInt()
    itemView.progress_current.max = progressItem.objective.toInt()

    handleExpiryDate(itemView.progress_expiry_date, itemView.progress_container_date,
        progressItem.endDate)
  }

}

class DefaultViewHolder(itemView: View,
                        private val clickListener: PublishSubject<String>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotions: Promotion) {
    val defaultItem = promotions as DefaultItem

    itemView.setOnClickListener {
      clickListener.onNext(promotions.id)
    }

    GlideApp.with(itemView.context)
        .load(defaultItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.default_icon)

    itemView.default_title.text = defaultItem.title
    handleExpiryDate(itemView.default_expiry_date, itemView.default_container_date,
        defaultItem.endDate)
  }

}

class FutureViewHolder(itemView: View,
                       private val clickListener: PublishSubject<String>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotions: Promotion) {
    val futureItem = promotions as FutureItem

    itemView.setOnClickListener {
      clickListener.onNext(promotions.id)
    }

    GlideApp.with(itemView.context)
        .load(futureItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.default_icon)

    itemView.default_title.text = futureItem.title
  }

}

class GamificationViewHolder(itemView: View,
                             private val clickListener: PublishSubject<String>) :
    PromotionsViewHolder(itemView) {

  private var mapper = GamificationMapper(itemView.context)

  override fun bind(promotions: Promotion) {
    val gamificationItem = promotions as GamificationItem
    val df = DecimalFormat("###.#")

    itemView.setOnClickListener {
      clickListener.onNext(promotions.id)
    }

    itemView.planet.setImageDrawable(gamificationItem.planet)
    itemView.current_level_bonus.background = mapper.getOvalBackground(gamificationItem.level)
    itemView.current_level_bonus.text =
        itemView.context?.getString(R.string.gamif_bonus, df.format(gamificationItem.bonus))
    itemView.planet_title.text = gamificationItem.title
    itemView.planet_subtitle.text = gamificationItem.phrase//TODO replace with

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