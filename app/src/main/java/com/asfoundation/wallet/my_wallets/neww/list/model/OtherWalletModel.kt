package com.asfoundation.wallet.my_wallets.neww.list.model

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.neww.list.OtherWalletsClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.google.android.material.card.MaterialCardView

@EpoxyModelClass
abstract class OtherWalletModel : EpoxyModelWithHolder<OtherWalletModel.OtherWalletHolder>() {

  @EpoxyAttribute
  lateinit var walletBalance: WalletBalance

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((OtherWalletsClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_other_wallet

  override fun bind(holder: OtherWalletHolder) {
    val balanceText = walletBalance.balance.symbol + currencyFormatUtils.formatCurrency(
        walletBalance.balance.amount)
    holder.walletAddress.text = walletBalance.walletAddress
    holder.walletBalance.text = balanceText

    holder.cardView.setOnClickListener {
      walletClickListener?.invoke(OtherWalletsClick.OtherWalletClick(walletBalance))
    }
  }


  class OtherWalletHolder : BaseViewHolder() {
    val cardView by bind<MaterialCardView>(R.id.card_view)
    val walletAddress by bind<TextView>(R.id.wallet_address_text_view)
    val walletBalance by bind<TextView>(R.id.wallet_balance_text_view)
  }
}