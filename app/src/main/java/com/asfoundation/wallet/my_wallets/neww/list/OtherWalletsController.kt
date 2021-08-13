package com.asfoundation.wallet.my_wallets.neww.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.my_wallets.neww.list.model.CreateNewWalletModel_
import com.asfoundation.wallet.my_wallets.neww.list.model.OtherWalletModel_
import com.asfoundation.wallet.my_wallets.neww.list.model.OtherWalletsTitleModel_
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.util.CurrencyFormatUtils

class OtherWalletsController : TypedEpoxyController<List<WalletBalance>>() {

  private val currencyFormatUtils = CurrencyFormatUtils()

  override fun buildModels(data: List<WalletBalance>) {
    if (data.isNotEmpty()) {
      add(OtherWalletsTitleModel_()
          .id("other_wallets_title_model"))
    }
    for (walletBalance in data) {
      add(
          OtherWalletModel_()
              .id("other_wallet_model_", walletBalance.walletAddress)
              .currencyFormatUtils(currencyFormatUtils)
              .walletBalance(walletBalance)
      )
    }
    add(CreateNewWalletModel_()
        .id("create_new_wallet_model"))
  }
}