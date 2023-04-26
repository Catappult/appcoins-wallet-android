package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

data class MergedAppcoinsBalance(val appcFiatValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue, val creditsFiatBalance: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue,
                                 val creditsAppcAmount: BigDecimal)

