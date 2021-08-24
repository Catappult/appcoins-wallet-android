package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.main.list.OtherWalletsClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class CreateNewWalletModel :
    EpoxyModelWithHolder<CreateNewWalletModel.CreateNewWalletHolder>() {

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((OtherWalletsClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_create_new_wallet

  override fun bind(holder: CreateNewWalletHolder) {
    super.bind(holder)
    holder.button.setOnClickListener {
      walletClickListener?.invoke(OtherWalletsClick.CreateNewWallet)
    }
  }

  class CreateNewWalletHolder : BaseViewHolder() {
    val button by bind<MaterialButton>(R.id.create_new_wallet_button)
  }
}