package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject

class WalletsAdapter(private val context: Context, private var items: List<WalletBalance>,
                     private val uiEventListener: PublishSubject<String>,
                     private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.Adapter<WalletsViewHolder>() {


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletsViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.wallet_rounded_outlined_card, parent, false)
    return WalletsViewHolder(context, view, uiEventListener, currencyFormatUtils)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(holder: WalletsViewHolder, position: Int) {
    holder.bind(items[position])
  }
}
