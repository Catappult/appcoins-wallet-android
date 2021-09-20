package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class UnverifiedModel : EpoxyModelWithHolder<UnverifiedModel.UnverifiedHolder>() {

  @EpoxyAttribute
  var disableButton: Boolean = false

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_unverified_wallet

  override fun bind(holder: UnverifiedHolder) {
    super.bind(holder)
    holder.verifyButton.setOnClickListener {
      walletClickListener?.invoke(WalletsListEvent.VerifyWalletClick)
    }
    holder.verifyButton.isEnabled = !disableButton
  }

  class UnverifiedHolder : BaseViewHolder() {
    val verifyButton by bind<MaterialButton>(R.id.verify_button)
  }
}