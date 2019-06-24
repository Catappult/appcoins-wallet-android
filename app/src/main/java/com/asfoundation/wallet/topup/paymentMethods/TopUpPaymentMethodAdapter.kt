package com.asfoundation.wallet.topup.paymentMethods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.jakewharton.rxrelay2.PublishRelay


class TopUpPaymentMethodAdapter(
    private var paymentMethods: List<PaymentMethodData>,
    private var paymentMethodClick: PublishRelay<String>) :
    RecyclerView.Adapter<PaymentMethodViewHolder>() {
  private var selectedItem = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
    return PaymentMethodViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.top_up_payment_method_item, parent, false))
  }

  override fun getItemCount(): Int {
    return paymentMethods.size
  }

  override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
    holder.bind(paymentMethods[position], selectedItem == position, View.OnClickListener {
      selectedItem = position
      paymentMethodClick.accept(paymentMethods[position].id)
      notifyDataSetChanged()
    })
  }

  fun getSelectedItemData(): PaymentMethodData {
    return paymentMethods[selectedItem]
  }
}