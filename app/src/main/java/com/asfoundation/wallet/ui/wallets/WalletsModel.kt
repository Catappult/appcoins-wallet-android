package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.iab.FiatValue

data class WalletsModel(val totalBalance: FiatValue, val totalWallets: Int,
                        val walletsBalance: List<WalletBalance>)
