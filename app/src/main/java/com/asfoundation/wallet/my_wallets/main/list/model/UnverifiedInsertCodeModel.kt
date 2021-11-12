package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class UnverifiedInsertCodeModel :
    EpoxyModelWithHolder<UnverifiedInsertCodeModel.UnverifiedInsertCodeHolder>() {

  @EpoxyAttribute
  var disableButton: Boolean = false

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_unverified_insert_code

  override fun bind(holder: UnverifiedInsertCodeHolder) {
    super.bind(holder)
    holder.insertCodeButton.setOnClickListener {
      walletClickListener?.invoke(WalletsListEvent.VerifyInsertCodeClick)
    }
    holder.insertCodeButton.isEnabled = !disableButton
  }

  class UnverifiedInsertCodeHolder : BaseViewHolder() {
    val insertCodeButton by bind<MaterialButton>(R.id.insert_code_button)
  }
}