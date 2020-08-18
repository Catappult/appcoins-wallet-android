package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.view.ViewGroup
import com.asfoundation.wallet.transactions.Transaction
import kotlinx.android.synthetic.main.item_transaction_promotion_bonus.view.*

class PromotionsBonusViewHolder(resId: Int, parent: ViewGroup) :
    BinderViewHolder<Transaction>(resId, parent) {

  override fun bind(data: Transaction?, addition: Bundle) {
    itemView.bonus_title.text = data?.title
    itemView.bonus_description.text = data?.description
  }

  companion object {
    const val VIEW_TYPE = 1010
  }
}