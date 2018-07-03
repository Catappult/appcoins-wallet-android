package com.asfoundation.wallet.ui.widget.adapter;

import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.widget.entity.SortedItem;
import java.util.List;

class ApplicationSortedItem extends SortedItem<List<AppcoinsApplication>> {
  public ApplicationSortedItem(List<AppcoinsApplication> value, int viewType) {
    super(viewType, value, Integer.MIN_VALUE);
  }

  @Override public int compare(SortedItem other) {
    return weight - other.weight;
  }

  @Override public boolean areContentsTheSame(SortedItem newItem) {
    return viewType == newItem.viewType && value.equals(newItem.value);
  }

  @Override public boolean areItemsTheSame(SortedItem other) {
    return viewType == other.viewType;
  }
}