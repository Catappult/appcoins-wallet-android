package com.asfoundation.wallet.ui.widget.adapter

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.entity.SortedItem

class CardNotificationSortedItem(value: List<CardNotification>, viewType: Int) :
    SortedItem<List<CardNotification>>(viewType, value, Integer.MIN_VALUE) {

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