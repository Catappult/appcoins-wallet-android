package com.asfoundation.wallet.ui.settings.change_currency.adapter

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrencyClickListener
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou


class ChangeFiatCurrencyItemViewHolder(itemView: View, val listener: FiatCurrencyClickListener) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

  private val fiatFlag: ImageView = itemView.findViewById(R.id.fiat_flag)
  private val shortCurrency: TextView = itemView.findViewById(R.id.fiat_currency_short)
  private val longCurrency: TextView = itemView.findViewById(R.id.fiat_currency_long)
  private val fiatCheckmark: ImageView = itemView.findViewById(R.id.fiat_check_mark)
  private val currencyItem: ConstraintLayout = itemView.findViewById(R.id.fiat_currency_item)

  init {
    itemView.setOnClickListener(this)
  }

  fun setCurrency(fiatCurrency: FiatCurrency, selected: Boolean) {
    Log.d("APPC-2472", "Holder -> setCurrency: parse flag " + Uri.parse(fiatCurrency.flag))
    GlideToVectorYou
        .init()
        .with(itemView.context)
        .setPlaceHolder(R.drawable.ic_currency, R.drawable.ic_currency)
        .load(Uri.parse(fiatCurrency.flag), fiatFlag)

    shortCurrency.text = fiatCurrency.currency
    longCurrency.text = fiatCurrency.label

    if (!selected) {
      currencyItem.setBackgroundColor(itemView.resources.getColor(R.color.white))
      fiatCheckmark.visibility = View.GONE
    } else {
      currencyItem.setBackgroundColor(
          itemView.resources.getColor(R.color.change_fiat_selected_item))
      fiatCheckmark.visibility = View.VISIBLE
    }
  }

  override fun onClick(view: View?) {
    listener.onClick(view, adapterPosition)
  }
}