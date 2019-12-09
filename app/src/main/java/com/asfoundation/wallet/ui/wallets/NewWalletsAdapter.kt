package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject

class NewWalletsAdapter(private val context: Context, private var items: List<WalletBalance>,
                        private val uiEventListener: PublishSubject<String>) :
    RecyclerView.Adapter<WalletsViewHolder>() {


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletsViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.other_wallet_card, parent, false)
    return WalletsViewHolder(context, view, uiEventListener)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(holder: WalletsViewHolder, position: Int) {
    holder.bind(items[position])
  }
}
