package com.asfoundation.wallet.promotions.voucher

import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject

class VoucherSkuViewHolder(private val button: Button) : RecyclerView.ViewHolder(button) {

  fun bind(position: Int, selectedPosition: Int, voucherSkuItem: VoucherSkuItem,
           onSkuClick: PublishSubject<Int>) {
    button.text = voucherSkuItem.title

    button.isActivated = selectedPosition == position

    button.setOnClickListener { onSkuClick.onNext(position) }
  }
}