package com.asfoundation.wallet.ui.wallets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject

class WalletsAdapter(
    private var items: List<WalletInfoSimple>,
    private val uiEventListener: PublishSubject<String>,
    private val currencyFormatUtils: CurrencyFormatUtils
) : RecyclerView.Adapter<WalletsViewHolder>() {


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletsViewHolder =
    WalletsViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(R.layout.wallet_rounded_outlined_card, parent, false),
      uiEventListener,
      currencyFormatUtils
    )

  override fun getItemCount(): Int = items.size

  override fun onBindViewHolder(holder: WalletsViewHolder, position: Int) =
    holder.bind(items[position])
}
