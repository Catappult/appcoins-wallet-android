package com.asfoundation.wallet.entity

data class GlobalBalance(val appcoinsBalance: Balance, val creditsBalance: Balance,
                         val etherBalance: Balance, val fiatSymbol: String,
                         val fiatValue: String, val showAppcoins: Boolean,
                         val showCredits: Boolean, val showEthereum: Boolean)

