package com.asfoundation.wallet.entity

import com.asfoundation.wallet.ui.iab.FiatValue

data class GlobalBalance(val appcoinsBalance: Balance, val appcoinsFiatValue: FiatValue,
                         val creditsBalance: Balance, val creditsFiatValue: FiatValue,
                         val etherBalance: Balance, val etherFiatValue: FiatValue)

