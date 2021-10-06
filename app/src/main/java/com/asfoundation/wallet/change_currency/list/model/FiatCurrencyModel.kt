package com.asfoundation.wallet.change_currency.list.model

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.change_currency.FiatCurrencyEntity
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@EpoxyModelClass
abstract class FiatCurrencyModel : EpoxyModelWithHolder<FiatCurrencyModel.FiatCurrencyHolder>() {

  @EpoxyAttribute
  lateinit var fiatCurrency: FiatCurrencyEntity

  @EpoxyAttribute
  var selected: Boolean = false

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((FiatCurrencyEntity) -> Unit)? = null

  override fun bind(holder: FiatCurrencyHolder) {
    GlideApp
        .with(holder.itemView.context)
        .load(Uri.parse(fiatCurrency.flag))
        .transition(DrawableTransitionOptions.withCrossFade())
        .circleCrop()
        .into(holder.fiatFlag)

    holder.shortCurrency.text = fiatCurrency.currency
    holder.longCurrency.text = fiatCurrency.label

    if (!selected) {
      holder.currencyItem.setBackgroundColor(holder.itemView.resources.getColor(R.color.white))
      holder.shortCurrency.setTextColor(
          holder.itemView.resources.getColor(R.color.black))
      holder.fiatCheckmark.visibility = View.GONE
    } else {
      holder.currencyItem.setBackgroundColor(
          holder.itemView.resources.getColor(R.color.change_fiat_selected_item))
      holder.shortCurrency.setTextColor(
          holder.itemView.resources.getColor(R.color.change_fiat_selected_item_short))
      holder.fiatCheckmark.visibility = View.VISIBLE
    }

    holder.itemView.setOnClickListener { clickListener?.invoke(fiatCurrency) }
  }

  override fun getDefaultLayout(): Int = R.layout.item_change_fiat_currency

  class FiatCurrencyHolder : BaseViewHolder() {
    val fiatFlag by bind<ImageView>(R.id.fiat_flag)
    val shortCurrency by bind<TextView>(R.id.fiat_currency_short)
    val longCurrency by bind<TextView>(R.id.fiat_currency_long)
    val fiatCheckmark by bind<ImageView>(R.id.fiat_check_mark)
    val currencyItem by bind<ConstraintLayout>(R.id.fiat_currency_item)
  }
}