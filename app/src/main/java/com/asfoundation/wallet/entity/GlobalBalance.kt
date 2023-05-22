package com.asfoundation.wallet.entity

import com.appcoins.wallet.feature.walletInfo.data.domain.WalletBalance

data class GlobalBalance(val walletBalance: com.appcoins.wallet.feature.walletInfo.data.domain.WalletBalance, val showAppcoins: Boolean,
                         val showCredits: Boolean, val showEthereum: Boolean)

