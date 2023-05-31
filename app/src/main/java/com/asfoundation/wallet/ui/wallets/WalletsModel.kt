package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.io.Serializable

data class WalletsModel(val totalBalance: FiatValue, val totalWallets: Int,
                        val wallets: List<WalletBalance>) : Serializable
