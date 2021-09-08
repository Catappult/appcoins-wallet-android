package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class BackupModel : EpoxyModelWithHolder<BackupModel.BackupHolder>() {

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_backup_wallet

  override fun bind(holder: BackupHolder) {
    super.bind(holder)
    holder.backupButton.setOnClickListener {
      walletClickListener?.invoke(WalletsListEvent.BackupClick)
    }
  }

  class BackupHolder : BaseViewHolder() {
    val backupButton by bind<MaterialButton>(R.id.backup_button)
  }
}