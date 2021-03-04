package com.asfoundation.wallet.promotions.voucher

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.voucher_details_sku_button.view.*

class VoucherSkuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(selectedPosition: Int, voucherSkuItem: VoucherSkuItem,
           onSkuClick: PublishSubject<Int>) {
    itemView.sku_button.text = voucherSkuItem.title

    itemView.sku_button.isActivated = selectedPosition == adapterPosition

    itemView.sku_button.setOnClickListener { onSkuClick.onNext(adapterPosition) }
  }

  fun refreshState(selectedPosition: Int) {
    itemView.sku_button.isActivated = selectedPosition == adapterPosition
  }
}