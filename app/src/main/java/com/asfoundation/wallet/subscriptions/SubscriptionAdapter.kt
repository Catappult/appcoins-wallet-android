package com.asfoundation.wallet.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject

class SubscriptionAdapter(private var clickListener: PublishSubject<String>?) :
    ListAdapter<SubscriptionItem, SubscriptionViewHolder>(
        object : DiffUtil.ItemCallback<SubscriptionItem>() {
          override fun areItemsTheSame(oldItem: SubscriptionItem, newItem: SubscriptionItem) =
              oldItem.appName == newItem.appName

          override fun areContentsTheSame(oldItem: SubscriptionItem, newItem: SubscriptionItem) =
              oldItem == newItem
        }
    ) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.subscription_item, parent, false)
    return SubscriptionViewHolder(view)
  }

  override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
    holder.bind(getItem(position), clickListener)
  }

}