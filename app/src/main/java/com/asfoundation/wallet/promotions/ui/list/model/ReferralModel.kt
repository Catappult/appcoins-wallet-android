package com.asfoundation.wallet.promotions.ui.list.model

import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.ReferralViewHolder
import com.asfoundation.wallet.promotions.model.ReferralItem
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency

@EpoxyModelClass
abstract class ReferralModel : EpoxyModelWithHolder<ReferralModel.ReferralHolder>() {

  @EpoxyAttribute
  lateinit var referralItem: ReferralItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PromotionClick) -> Unit)? = null

  override fun bind(holder: ReferralHolder) {
    val context = holder.itemView.context

    holder.itemView.setOnClickListener {
      val extras = mapOf(
          Pair(ReferralViewHolder.KEY_LINK, referralItem.link),
          Pair(ReferralViewHolder.KEY_ACTION, ReferralViewHolder.ACTION_DETAILS)
      )
      clickListener?.invoke(PromotionClick(referralItem.id, extras))
    }

    holder.shareContainer.setOnClickListener {
      val extras = mapOf(
          Pair(ReferralViewHolder.KEY_LINK, referralItem.link),
          Pair(ReferralViewHolder.KEY_ACTION, ReferralViewHolder.ACTION_SHARE)
      )
      clickListener?.invoke(PromotionClick(referralItem.id, extras))
    }

    val bonus = currencyFormatUtils.formatCurrency(referralItem.bonus, WalletCurrency.FIAT)

    val subtitle = context.getString(R.string.promotions_referral_card_title,
        referralItem.currency + bonus)

    holder.referralSubtitle.text = subtitle
  }

  override fun getDefaultLayout(): Int = R.layout.item_promotions_referrals

  class ReferralHolder : BaseViewHolder() {
    val shareContainer by bind<LinearLayout>(R.id.share_container)
    val referralSubtitle by bind<TextView>(R.id.referral_subtitle)
  }
}