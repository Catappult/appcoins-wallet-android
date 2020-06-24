package com.asfoundation.wallet.topup.paymentMethods

import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import kotlinx.android.synthetic.main.top_up_payment_method_item.view.*

class PaymentMethodViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(data: PaymentMethodData, checked: Boolean, listener: View.OnClickListener) {

    GlideApp.with(itemView.context)
        .load(data.imageSrc)
        .into(itemView.payment_method_ic)
    itemView.payment_method_description.text = data.description
    itemView.radio_button.isChecked = checked
    itemView.setOnClickListener(listener)

    if (checked) {
      itemView.payment_method_description.setTextColor(
          ContextCompat.getColor(itemView.context!!, R.color.details_address_text_color))
      itemView.payment_method_description.typeface =
          Typeface.create("sans-serif-medium", Typeface.NORMAL)
    } else {
      itemView.payment_method_description.setTextColor(
          ContextCompat.getColor(itemView.context!!, R.color.grey_alpha_active_54))
      itemView.payment_method_description.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
  }
}

data class PaymentMethodData(val imageSrc: String, val description: String, val id: String)