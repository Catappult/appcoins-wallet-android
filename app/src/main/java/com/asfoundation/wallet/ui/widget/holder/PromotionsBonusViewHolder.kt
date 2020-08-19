package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import kotlinx.android.synthetic.main.item_transaction_promotion_bonus.view.*

class PromotionsBonusViewHolder(resId: Int, parent: ViewGroup) :
    BinderViewHolder<Transaction>(resId, parent) {

  override fun bind(data: Transaction?, addition: Bundle) {
    handleIcon(data?.details)
    itemView.bonus_title.text = data?.title
    itemView.bonus_description.text = data?.description
  }

  private fun handleIcon(details: TransactionDetails?) {
    var uri: String? = null
    val icon: TransactionDetails.Icon?
    if (details != null) {
      icon = details.icon
      when (icon?.type) {
        TransactionDetails.Icon.Type.FILE -> uri = "file:" + icon.uri
        TransactionDetails.Icon.Type.URL -> uri = icon.uri
      }
    }
    GlideApp.with(context)
        .load(uri)
        .error(R.drawable.transactions_promotion_bonus)
        .into(itemView.img)
  }

  companion object {
    const val VIEW_TYPE = 1010
  }
}