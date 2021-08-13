package com.asfoundation.wallet.my_wallets.neww.list.model

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.util.CurrencyFormatUtils

@EpoxyModelClass
abstract class OtherWalletModel : EpoxyModelWithHolder<OtherWalletModel.OtherWalletHolder>() {

  @EpoxyAttribute
  lateinit var walletBalance: WalletBalance

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  override fun getDefaultLayout(): Int = R.layout.item_other_wallet

  override fun bind(holder: OtherWalletHolder) {
    val balanceText = walletBalance.balance.symbol + currencyFormatUtils.formatCurrency(
        walletBalance.balance.amount)
    holder.walletAddress.text = walletBalance.walletAddress
    holder.walletBalance.text = balanceText
  }


  class OtherWalletHolder : BaseViewHolder() {
    val walletAddress by bind<TextView>(R.id.wallet_address_text_view)
    val walletBalance by bind<TextView>(R.id.wallet_balance_text_view)
  }
}