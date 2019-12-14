package com.asfoundation.wallet.subscriptions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.math.RoundingMode

class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: ((SubscriptionItem) -> Unit)?) {
    itemView.apply {
      app_name.text = item.appName
      monthly_value.text = String.format("%s / month",
          item.symbol + item.amount.setScale(FIAT_SCALE, RoundingMode.FLOOR))
      more_button.setOnClickListener { clickCallback?.invoke(item) }
      item_parent.setOnClickListener { clickCallback?.invoke(item) }
    }

    Picasso.with(itemView.context)
        .load(item.iconUrl)
        .error(R.drawable.ic_transaction_peer)//TODO
        .placeholder(R.drawable.ic_transaction_peer)//TODO
        .into(itemView.app_icon)
  }

  companion object {
    private const val FIAT_SCALE = 2
  }

}