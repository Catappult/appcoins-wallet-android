package com.asfoundation.wallet.home.ui.list.transactions

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.asf.wallet.R

@EpoxyModelClass
abstract class LoadingModel : EpoxyModelWithHolder<LoadingModel.LoadingHolder>() {

  override fun getDefaultLayout(): Int = R.layout.item_loading

  class LoadingHolder : BaseViewHolder()
}