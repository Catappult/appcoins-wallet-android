package com.asfoundation.wallet.my_wallets.neww.list

import com.asfoundation.wallet.ui.wallets.WalletBalance

sealed class OtherWalletsClick {
  data class OtherWalletClick(val walletBalance: WalletBalance) : OtherWalletsClick()
  object CreateNewWallet : OtherWalletsClick()
}