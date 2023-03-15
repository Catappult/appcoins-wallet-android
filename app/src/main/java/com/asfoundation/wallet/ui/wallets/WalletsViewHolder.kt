package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.wallet_rounded_outlined_card.view.*

class WalletsViewHolder(
  itemView: View,
  private val uiEventListener: PublishSubject<String>,
  private val currencyFormatUtils: CurrencyFormatUtils
) : RecyclerView.ViewHolder(itemView) {

  @SuppressLint("SetTextI18n")
  fun bind(item: WalletBalance) {
    itemView.wallet_address.text = item.walletName
    itemView.wallet_balance.text =
      "${item.balance.symbol}${currencyFormatUtils.formatCurrency(item.balance.amount)}"
    itemView.setOnClickListener { uiEventListener.onNext(item.walletAddress) }
  }
}
