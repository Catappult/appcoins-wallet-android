package com.asfoundation.wallet.topup.paymentMethods

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.iab.TranslatablePaymentMethods
import kotlinx.android.synthetic.main.top_up_payment_method_item.view.*

class PaymentMethodViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(data: PaymentMethodData, checked: Boolean, listener: View.OnClickListener) {

    GlideApp.with(itemView.context)
        .load(data.imageSrc)
        .into(itemView.payment_method_ic)
    itemView.payment_method_description.text = getPaymentMethodLabel(data)
    itemView.payment_method_description.isEnabled = data.isAvailable
    itemView.radio_button.isChecked = checked && data.isAvailable
    itemView.radio_button.isEnabled = data.isAvailable
    if (data.isAvailable) itemView.setOnClickListener(listener)
    else itemView.background = null //Remove ripple effect
  }

  private fun getPaymentMethodLabel(paymentMethod: PaymentMethodData): String {
    return TranslatablePaymentMethods.values()
        .firstOrNull { it.paymentMethod == paymentMethod.id }
        ?.let { itemView.context.getString(it.stringId) } ?: paymentMethod.description
  }
}

data class PaymentMethodData(val imageSrc: String, val description: String,
                             val id: String, val isAvailable: Boolean)