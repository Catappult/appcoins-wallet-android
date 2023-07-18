package com.asfoundation.wallet.backup.entryBottomSheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.backup.ui.R
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import io.reactivex.subjects.PublishSubject

class BackupEntryChooseWalletAdapter(
    private var items: List<WalletInfoSimple>,
    private val uiEventListener: PublishSubject<String>,
    private val currencyFormatUtils: CurrencyFormatUtils,
) : RecyclerView.Adapter<BackupEntryChooseWalletViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupEntryChooseWalletViewHolder =
    BackupEntryChooseWalletViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(R.layout.wallet_rounded_outlined_card, parent, false),
      uiEventListener,
      currencyFormatUtils
    )

  override fun getItemCount(): Int = items.size

  override fun onBindViewHolder(holder: BackupEntryChooseWalletViewHolder, position: Int) {
    val currentItem = items[position]
    holder.bind(currentItem)

    val currentWalletIcon = holder.itemView.findViewById<ImageView>(R.id.ic_wallet_address)
    val currentWalletRound = holder.itemView.findViewById<View>(R.id.outline)
    if(!currentItem.backupWalletActive) {
      currentWalletIcon.visibility = View.INVISIBLE
      currentWalletRound.visibility = View.INVISIBLE
    }else{
      currentWalletIcon.visibility = View.VISIBLE
      currentWalletRound.visibility = View.VISIBLE
    }
  }
  }

