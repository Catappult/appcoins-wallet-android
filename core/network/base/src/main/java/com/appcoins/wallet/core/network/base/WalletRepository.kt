package com.appcoins.wallet.core.network.base

interface WalletRepository {

  fun getDefaultWalletAddress(): String
}
