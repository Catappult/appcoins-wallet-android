package com.appcoins.wallet.feature.walletInfo.data.wallet.domain

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import java.io.Serializable

data class WalletsModel(
  val totalBalance: FiatValue, val totalWallets: Int,
  val wallets: List<WalletInfoSimple>
) : Serializable

fun WalletsModel.activeWalletAddress() = wallets.first { it.isActiveWallet }.walletAddress

fun WalletsModel.inactiveWallets() = wallets.filter { !it.isActiveWallet }