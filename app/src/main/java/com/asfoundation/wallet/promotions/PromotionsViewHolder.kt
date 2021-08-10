import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.promotions.PromotionsGamificationAdapter
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_INFO
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.addBottomItemDecoration
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_promotions_future.view.*
import kotlinx.android.synthetic.main.item_promotions_gamification.view.*
import kotlinx.android.synthetic.main.item_promotions_progress.view.*
import kotlinx.android.synthetic.main.item_promotions_referrals.view.*
import kotlinx.android.synthetic.main.item_promotions_vouchers.view.*
import kotlinx.android.synthetic.main.perks_content.view.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

abstract class PromotionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  companion object {
    const val DETAILS_URL_EXTRA = "DETAILS_URL_EXTRA"
    const val PACKAGE_NAME_EXTRA = "PACKAGE_NAME_EXTRA"
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
      days in 1..3 -> updateDate(view, container, days, R.plurals.promotion_ends_short)
      hours > 0 -> updateDate(view, container, hours, R.plurals.promotion_ends_hours_short)
      else -> updateDate(view, container, minutes, R.plurals.promotion_ends_minutes_short)
    }
  }

  protected fun loadIcon(icon: String?, into: ImageView) {
    GlideApp.with(itemView.context)
        .load(icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(into)

  }

  private fun updateDate(view: TextView, container: LinearLayout, time: Long,
                         @PluralsRes text: Int) {
    container.visibility = View.VISIBLE
    view.text = itemView.context.resources.getQuantityString(text, time.toInt(), time.toString())
  }
}

class ProgressViewHolder(itemView: View,
                         private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val progressItem = promotion as ProgressItem

    itemView.isClickable = progressItem.detailsLink != null

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      progressItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener.onNext(PromotionClick(promotion.id, extras))
    }

    loadIcon(progressItem.icon, itemView.active_icon)

    itemView.active_title.text = progressItem.description

    if (progressItem.objective != null) setProgressWithObjective(progressItem)
    else setMaxProgress(progressItem)

    handleExpiryDate(itemView.active_expiry_date, itemView.active_container_date,
        progressItem.endDate)
  }

  private fun setProgressWithObjective(progressItem: ProgressItem) {
    itemView.progress_current.max = progressItem.objective!!.toInt()
    itemView.progress_current.progress = progressItem.current.toInt()
    val progress = "${progressItem.current.toInt()}/${progressItem.objective.toInt()}"
    itemView.progress_label.text = progress
  }

  private fun setMaxProgress(progressItem: ProgressItem) {
    itemView.progress_current.max = progressItem.current.toInt()
    itemView.progress_current.progress = progressItem.current.toInt()
    itemView.progress_label.text = "${progressItem.current.toInt()}"
  }
}

class DefaultViewHolder(itemView: View,
                        private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val defaultItem = promotion as DefaultItem

    itemView.isClickable = defaultItem.detailsLink != null

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      defaultItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener.onNext(PromotionClick(promotion.id, extras))
    }

    loadIcon(defaultItem.icon, itemView.active_icon)

    itemView.active_title.text = defaultItem.description
    handleExpiryDate(itemView.active_expiry_date, itemView.active_container_date,
        defaultItem.endDate)
  }

}

class FutureViewHolder(itemView: View,
                       private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val futureItem = promotion as FutureItem

    itemView.isClickable = futureItem.detailsLink != null

    itemView.setOnClickListener {
      val extras = emptyMap<String, String>().toMutableMap()
      futureItem.detailsLink?.let {
        extras[DETAILS_URL_EXTRA] = it
      }
      clickListener.onNext(PromotionClick(promotion.id, extras))
    }

    loadIcon(futureItem.icon, itemView.future_icon)

    itemView.future_title.text = futureItem.description
  }

}

class ReferralViewHolder(itemView: View,
                         private val clickListener: PublishSubject<PromotionClick>) :
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
      clickListener.onNext(PromotionClick(promotion.id, extras))
    }

    itemView.share_container.setOnClickListener {
      val extras = mapOf(
          Pair(KEY_LINK, referralItem.link),
          Pair(KEY_ACTION, ACTION_SHARE)
      )
      clickListener.onNext(PromotionClick(promotion.id, extras))
    }

    val formatter = CurrencyFormatUtils.create()
    val bonus = formatter.formatCurrency(referralItem.bonus, WalletCurrency.FIAT)

    val subtitle = itemView.context.getString(R.string.promotions_referral_card_title,
        referralItem.currency + bonus)

    itemView.referral_subtitle.text = subtitle
  }

}

class GamificationViewHolder(itemView: View,
                             private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  init {
    itemView.linked_perks.addBottomItemDecoration(
        itemView.resources.getDimension(R.dimen.promotions_item_margin))
  }

  private var mapper = GamificationMapper(itemView.context)

  override fun bind(promotion: Promotion) {
    val gamificationItem = promotion as GamificationItem
    val formatter = CurrencyFormatUtils.create()
    val df = DecimalFormat("###.#")
    val bonus = formatter.formatGamificationValues(BigDecimal(gamificationItem.maxBonus))

    itemView.gamification_title.text = itemView.context.getString(R.string.perks_gamif_title, bonus)
    itemView.setOnClickListener { clickListener.onNext(PromotionClick(promotion.id)) }
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

    itemView.gamification_info_btn.setOnClickListener {
      clickListener.onNext(PromotionClick(GAMIFICATION_INFO))
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

class VouchersViewHolder(itemView: View,
                         private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val voucher = promotion as VoucherItem
    itemView.voucher_app_name.text = voucher.title
    if (voucher.hasAppcoins) itemView.has_appcoins_view.visibility = View.VISIBLE
    else itemView.has_appcoins_view.visibility = View.GONE

    if (voucher.maxBonus != 0.0) {
      itemView.has_bonus_group.visibility = View.VISIBLE
      val formatter = CurrencyFormatUtils.create()
      val bonus = formatter.formatGamificationValues(BigDecimal(voucher.maxBonus))
      itemView.voucher_description.text =
          String.format(itemView.context.getString(R.string.voucher_card_body_1), bonus)
    } else {
      itemView.has_bonus_group.visibility = View.GONE
    }

    loadIcon(voucher.icon, itemView.voucher_icon)

    itemView.setOnClickListener {
      //TODO add here more info needed to identify app to move to details fragment
      val extras = mapOf(Pair(PACKAGE_NAME_EXTRA, voucher.packageName))
      clickListener.onNext(PromotionClick(voucher.id, extras))
    }
  }
}