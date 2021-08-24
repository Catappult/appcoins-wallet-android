package com.asfoundation.wallet.my_wallets.main.list.model

import android.widget.TextView
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder

@EpoxyModelClass
abstract class OtherWalletsTitleModel :
    EpoxyModelWithHolder<OtherWalletsTitleModel.OtherWalletsTitleHolder>() {

  override fun getDefaultLayout(): Int = R.layout.item_other_wallets_title

  class OtherWalletsTitleHolder : BaseViewHolder() {
    val title by bind<TextView>(R.id.title)
  }
}