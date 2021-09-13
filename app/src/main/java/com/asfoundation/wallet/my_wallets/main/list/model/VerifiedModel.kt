package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder

@EpoxyModelClass
abstract class VerifiedModel : EpoxyModelWithHolder<VerifiedModel.VerifiedHolder>() {

  override fun getDefaultLayout(): Int = R.layout.item_verified_wallet

  class VerifiedHolder : BaseViewHolder()
}