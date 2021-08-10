package com.asfoundation.wallet.promotions

import PromotionsViewHolder
import VouchersViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.model.VoucherItem
import com.asfoundation.wallet.promotions.ui.list.PromotionsAdapter
import io.reactivex.subjects.PublishSubject


class VouchersAdapter(vouchers: List<VoucherItem>,
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