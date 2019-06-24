package com.asfoundation.wallet.topup.paymentMethods

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.top_up_payment_method_item.view.*

class PaymentMethodViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(data: PaymentMethodData, checked: Boolean, listener: View.OnClickListener) {
    Picasso.with(itemView.context)
        .load(data.imageSrc)
        .into(itemView.payment_method_ic)
    itemView.payment_method_description.text = data.description
    itemView.radio_button.isChecked = checked
    itemView.setOnClickListener(listener)
  }
}

data class PaymentMethodData(val imageSrc: String, val description: String, val id: String)