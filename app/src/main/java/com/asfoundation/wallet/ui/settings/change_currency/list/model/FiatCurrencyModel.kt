package com.asfoundation.wallet.ui.settings.change_currency.list.model

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency

@EpoxyModelClass
abstract class FiatCurrencyModel : EpoxyModelWithHolder<FiatCurrencyModel.FiatCurrencyHolder>() {

  @EpoxyAttribute
  lateinit var fiatCurrency: FiatCurrency

  @EpoxyAttribute
  var selected: Boolean = false

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((FiatCurrency) -> Unit)? = null

  override fun bind(holder: FiatCurrencyHolder) {
    // Bind

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