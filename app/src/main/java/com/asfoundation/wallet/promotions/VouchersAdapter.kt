package com.asfoundation.wallet.promotions

import android.view.LayoutInflater
import android.view.ViewGroup
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject


class VouchersAdapter(vouchers: List<Voucher>,
                      private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsAdapter() {

  init {
    currentList = vouchers
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionsViewHolder {
    val layout = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_promotions_vouchers, parent, false)
    return VouchersViewHolder(layout, clickListener)
  }
}