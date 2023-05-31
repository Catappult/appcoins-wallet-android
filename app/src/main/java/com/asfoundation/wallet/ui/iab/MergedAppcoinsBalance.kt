package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.math.BigDecimal

data class MergedAppcoinsBalance(val appcFiatValue: FiatValue, val creditsFiatBalance: FiatValue,
                                 val creditsAppcAmount: BigDecimal)

