package com.asfoundation.wallet.home.ui.list.transactions.empty

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.home.ui.list.HomeListClick

class EmptyTransactionsController : TypedEpoxyController<List<EmptyItem>>() {

  var clickListener: ((HomeListClick) -> Unit)? = null

  override fun buildModels(data: List<EmptyItem>) {
    for (item in data) {
      add(
          EmptyItemModel_()
              .id(item.id)
              .emptyItem(item)
              .clickListener(clickListener)
      )
    }

  }
}