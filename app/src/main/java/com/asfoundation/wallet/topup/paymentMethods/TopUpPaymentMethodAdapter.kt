package com.asfoundation.wallet.topup.paymentMethods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R


class TopUpPaymentMethodAdapter(private var paymentMethods: MutableList<PaymentMethodData>) :
    RecyclerView.Adapter<PaymentMethodViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
    return PaymentMethodViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.top_payment_method_item, parent, false))
  }

  override fun getItemCount(): Int {
    return paymentMethods.size
  }

  override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
//    holder.bind(paymentMethods[position])
  }


}