package com.asfoundation.wallet.my_wallets.main.list

import com.asfoundation.wallet.ui.wallets.WalletBalance

sealed class OtherWalletsClick {
  data class OtherWalletClick(val walletBalance: WalletBalance) : OtherWalletsClick()
  object CreateNewWallet : OtherWalletsClick()
}