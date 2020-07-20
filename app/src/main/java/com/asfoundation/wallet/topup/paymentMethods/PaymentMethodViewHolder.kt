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
    itemView.payment_method_description.text = setPaymentMethodLabel(data)
    itemView.radio_button.isChecked = checked
    itemView.setOnClickListener(listener)
  }

  private fun setPaymentMethodLabel(paymentMethod: PaymentMethodData): String {
    if (TranslatablePaymentMethods.values()
            .any { it.paymentMethod == paymentMethod.id }) {
      TranslatablePaymentMethods.values()
          .first { it.paymentMethod == paymentMethod.id }
          .let { return itemView.context.getString(it.stringId) }
    }
    return paymentMethod.description
  }
}

data class PaymentMethodData(val imageSrc: String, val description: String, val id: String)