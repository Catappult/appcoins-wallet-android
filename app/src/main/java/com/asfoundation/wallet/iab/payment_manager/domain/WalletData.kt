package com.asfoundation.wallet.iab.payment_manager.domain

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.emptyWalletInfo

class WalletData(
  val address: String,
  val ewt: String,
  val walletInfo: WalletInfo,
)

val emptyWalletData = WalletData(
  address = "",
  ewt = "",
  walletInfo = emptyWalletInfo
)
