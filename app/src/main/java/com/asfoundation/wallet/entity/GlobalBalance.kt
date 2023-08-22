package com.asfoundation.wallet.entity

import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance


data class GlobalBalance(val walletBalance: WalletBalance, val showAppcoins: Boolean,
                         val showCredits: Boolean, val showEthereum: Boolean)

