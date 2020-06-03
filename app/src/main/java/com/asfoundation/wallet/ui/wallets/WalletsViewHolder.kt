package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.other_wallet_card.view.*
import kotlinx.android.synthetic.main.other_wallet_card.view.wallet_balance
import kotlinx.android.synthetic.main.wallet_rounded_outlined_card.view.*

class WalletsViewHolder(private val context: Context, itemView: View,
                        private val uiEventListener: PublishSubject<String>,
                        private val currencyFormatUtils: CurrencyFormatUtils,
                        private val walletsViewType: WalletsViewType) :
    RecyclerView.ViewHolder(itemView) {

  @SuppressLint("SetTextI18n")
  fun bind(item: WalletBalance) {
    if (walletsViewType == WalletsViewType.BALANCE) {
      itemView.inactive_wallet_address.text = item.walletAddress
      itemView.wallet_balance.text = context.getString(
          R.string.wallets_2nd_view_balance_title) + " " + item.balance.symbol + currencyFormatUtils.formatCurrency(
          item.balance.amount)
    } else if (walletsViewType == WalletsViewType.SETTINGS) {
      itemView.wallet_address.text = item.walletAddress
      itemView.wallet_balance.text =
          "${item.balance.symbol}${currencyFormatUtils.formatCurrency(item.balance.amount)}"
    }
    itemView.setOnClickListener { uiEventListener.onNext(item.walletAddress) }
  }
}
