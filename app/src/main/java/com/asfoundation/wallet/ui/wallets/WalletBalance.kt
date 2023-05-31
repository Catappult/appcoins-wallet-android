package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.io.Serializable

data class WalletBalance(
  val walletName: String,
  val walletAddress: String,
  val balance: FiatValue,
  val isActiveWallet: Boolean,
  val backupDate: Long
) : Serializable {

  constructor() : this("", "", FiatValue(), false, 0)
}
