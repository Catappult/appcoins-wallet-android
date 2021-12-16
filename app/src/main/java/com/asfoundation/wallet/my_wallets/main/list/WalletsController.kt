package com.asfoundation.wallet.my_wallets.main.list

import com.airbnb.epoxy.Typed4EpoxyController
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.my_wallets.main.list.model.ActiveWalletModelGroup
import com.asfoundation.wallet.my_wallets.main.list.model.CreateNewWalletModel_
import com.asfoundation.wallet.my_wallets.main.list.model.OtherWalletModel_
import com.asfoundation.wallet.my_wallets.main.list.model.OtherWalletsTitleModel_
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.domain.WalletInfo

class WalletsController :
    Typed4EpoxyController<Async<WalletsModel>, Async<BalanceVerificationModel>, Async<WalletInfo>, Async<Boolean>>() {

  private val currencyFormatUtils = CurrencyFormatUtils()

  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun buildModels(walletsAsync: Async<WalletsModel>,
                           walletVerifiedAsync: Async<BalanceVerificationModel>,
                           walletInfoAsync: Async<WalletInfo>,
                           backedUpOnceAsync: Async<Boolean>) {
    add(ActiveWalletModelGroup(walletVerifiedAsync, walletInfoAsync,
        backedUpOnceAsync, currencyFormatUtils, walletClickListener))
    addOtherWallets(walletsAsync)
  }

  private fun addOtherWallets(walletsAsync: Async<WalletsModel>) {
    val otherWallets = walletsAsync()?.wallets
    if (otherWallets != null && otherWallets.isNotEmpty()) {
      add(OtherWalletsTitleModel_()
          .id("other_wallets_title_model"))

      for (walletBalance in otherWallets) {
        if (!walletBalance.isActiveWallet) {
          add(
              OtherWalletModel_()
                  .id("other_wallet_model_", walletBalance.walletAddress)
                  .currencyFormatUtils(currencyFormatUtils)
                  .walletBalance(walletBalance)
                  .walletClickListener(walletClickListener)
          )
        }
      }
    }

    add(CreateNewWalletModel_()
        .id("create_new_wallet_model")
        .walletClickListener(walletClickListener))
  }
}