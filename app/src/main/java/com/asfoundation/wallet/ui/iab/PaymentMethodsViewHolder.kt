package com.asfoundation.wallet.ui.iab

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.GlideApp
import kotlinx.android.synthetic.main.payment_method_item.view.*

class PaymentMethodViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(data: PaymentMethod, checked: Boolean, listener: View.OnClickListener) {

    GlideApp.with(itemView.context)
        .load(data.iconUrl)
        .into(itemView.payment_method_ic)
    itemView.payment_method_description.text = data.label
    itemView.radio_button.isChecked = data.isEnabled && checked
    itemView.radio_button.isEnabled = data.isEnabled
    itemView.setOnClickListener(listener)
    if (true) {//TODO needs to be changed after implemented by microservices
      itemView.fee_label.visibility = View.VISIBLE
      itemView.fee_label.text = "1.5 €"
    } else {
      itemView.fee_label.visibility = View.GONE
    }
  }
}