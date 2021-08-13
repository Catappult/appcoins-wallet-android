package com.asfoundation.wallet.my_wallets.neww.list.model

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class CreateNewWalletModel :
    EpoxyModelWithHolder<CreateNewWalletModel.CreateNewWalletHolder>() {

  override fun getDefaultLayout(): Int = R.layout.item_create_new_wallet

  override fun bind(holder: CreateNewWalletHolder) {
    super.bind(holder)
  }

  class CreateNewWalletHolder : BaseViewHolder() {
    val button by bind<MaterialButton>(R.id.create_new_wallet_button)
  }
}