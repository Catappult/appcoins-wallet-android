package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

data class MergedAppcoinsBalance(val appcFiatValue: FiatValue, val creditsFiatBalance: FiatValue,
                                 val creditsAppcAmount: BigDecimal)

