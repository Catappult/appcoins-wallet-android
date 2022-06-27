package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.iab.FiatValue
import java.io.Serializable

data class WalletBalance(
  val walletAddress: String,
  val balance: FiatValue,
  val isActiveWallet: Boolean
) : Serializable {

  constructor() : this("", FiatValue(), false)
}
