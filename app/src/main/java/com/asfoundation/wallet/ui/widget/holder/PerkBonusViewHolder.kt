package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener
import kotlinx.android.synthetic.main.item_transaction_perk_bonus.view.*

class PerkBonusViewHolder(resId: Int,
                          parent: ViewGroup,
                          private val onTransactionClickListener: OnTransactionClickListener) :
    BinderViewHolder<Transaction>(resId, parent) {

  override fun bind(data: Transaction?, addition: Bundle) {
    handleIcon(data?.details)

    if (!data?.title.isNullOrEmpty()) itemView.bonus_title.text = data?.title
    else itemView.bonus_title.visibility = View.GONE

    itemView.bonus_description.text = data?.description
    itemView.setOnClickListener { onClick(it, data) }
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

  private fun onClick(view: View, transaction: Transaction?) {
    transaction?.let { onTransactionClickListener.onTransactionClick(view, transaction) }
  }

  companion object {
    const val VIEW_TYPE = 1010
  }
}