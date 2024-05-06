package com.asfoundation.wallet.viewmodel

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.entity.NetworkInfo

data class TransactionsWalletModel(
  val networkInfo: NetworkInfo, val wallet: Wallet,
  val isNewWallet: Boolean
)