package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.iab.FiatValue

data class WalletBalance(val walletAddress: String, val balance: FiatValue,
                         val isActiveWallet: Boolean) {

  constructor() : this("", FiatValue(), false)
}
