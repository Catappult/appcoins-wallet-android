package com.asfoundation.wallet.ui.widget.adapter

import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.ui.widget.entity.SortedItem

class ReferralNotificationSortedItem(value: List<ReferralNotification>, viewType: Int) :
    SortedItem<List<ReferralNotification>>(viewType, value, Integer.MIN_VALUE) {

  override fun compare(other: SortedItem<*>): Int {
    return weight - other.weight
  }

  override fun areContentsTheSame(newItem: SortedItem<*>): Boolean {
    return viewType == newItem.viewType && value == newItem.value
  }

  override fun areItemsTheSame(other: SortedItem<*>): Boolean {
    return viewType == other.viewType
  }
}