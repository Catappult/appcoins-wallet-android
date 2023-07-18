package com.asfoundation.wallet.backup.entryBottomSheet

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.maskedEnd
import com.appcoins.wallet.feature.backup.ui.databinding.WalletRoundedOutlinedCardBinding
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import io.reactivex.subjects.PublishSubject

class BackupEntryChooseWalletViewHolder(
  itemView: View,
  private val uiEventListener: PublishSubject<String>,
  private val currencyFormatUtils: CurrencyFormatUtils
) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { WalletRoundedOutlinedCardBinding.bind(itemView) }

  @SuppressLint("SetTextI18n")
  fun bind(item: WalletInfoSimple) {
    binding.walletName.text = "${item.walletName} - "
    binding.walletAddress.text = item.walletAddress.maskedEnd()
    binding.walletBalance.text =
      "${item.balance.symbol}${currencyFormatUtils.formatCurrency(item.balance.amount)} ${item.balance.currency}"
    itemView.setOnClickListener { uiEventListener.onNext(item.walletAddress)
    }
  }
}
