package com.appcoins.wallet.feature.walletInfo.data.balance

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.io.Serializable

/**
 * This name should be changed this is for mapping walletInfo to simplified version
 */
data class WalletInfoSimple(
  val walletName: String,
  val walletAddress: String,
  val balance: FiatValue,
  val isActiveWallet: Boolean,
  val backupDate: Long
) : Serializable {

  constructor() : this(
    "", "",
    FiatValue(), false, 0
  )
}
