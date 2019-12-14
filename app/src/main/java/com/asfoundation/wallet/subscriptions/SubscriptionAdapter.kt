package com.asfoundation.wallet.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.asf.wallet.R

class SubscriptionAdapter(
    private val clickCallback: ((SubscriptionItem) -> Unit)?
) : ListAdapter<SubscriptionItem, SubscriptionViewHolder>(
    object : DiffUtil.ItemCallback<SubscriptionItem>() {
      override fun areItemsTheSame(oldItem: SubscriptionItem, newItem: SubscriptionItem): Boolean {
        return oldItem.appName == newItem.appName
      }

      override fun areContentsTheSame(oldItem: SubscriptionItem,
                                      newItem: SubscriptionItem): Boolean {
        return oldItem.appName == newItem.appName &&
            oldItem.amount.compareTo(newItem.amount) != 0 &&
            oldItem.symbol.compareTo(newItem.symbol) != 0 &&
            oldItem.iconUrl == newItem.iconUrl
      }
    }
) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.subscription_item, parent, false)
    return SubscriptionViewHolder(view)
  }

  override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
    holder.bind(getItem(position), clickCallback)
  }

}