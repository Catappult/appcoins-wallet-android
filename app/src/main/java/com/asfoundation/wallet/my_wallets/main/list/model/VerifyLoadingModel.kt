package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder

@EpoxyModelClass
abstract class VerifyLoadingModel : EpoxyModelWithHolder<VerifyLoadingModel.LoadingHolder>() {

  override fun getDefaultLayout(): Int = R.layout.item_verify_loading

  class LoadingHolder : BaseViewHolder()
}