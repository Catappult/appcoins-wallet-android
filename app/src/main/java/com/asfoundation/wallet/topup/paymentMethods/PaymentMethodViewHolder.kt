package com.asfoundation.wallet.topup.paymentMethods

import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.asf.wallet.R

/**
 * Created by Joao Raimundo on 13/02/2019.
 */
class PaymentMethodViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
  private val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
  private val icon: ImageView = itemView.findViewById(R.id.payment_method_ic)
  private val text: TextView = itemView.findViewById(R.id.payment_method_description)

  fun bind(data: PaymentMethodData) {
    Picasso.with(itemView.context)
        .load(data.imageSrc)
        .into(icon)
    text.text = data.description
    radioButton.isEnabled = true
  }
}

class PaymentMethodData(val imageSrc: String, val description: String)
