package com.asfoundation.wallet.entity

import com.asfoundation.wallet.wallets.domain.WalletBalance

data class GlobalBalance(val walletBalance: WalletBalance, val showAppcoins: Boolean,
                         val showCredits: Boolean, val showEthereum: Boolean)

