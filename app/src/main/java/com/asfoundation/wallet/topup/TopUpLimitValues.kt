package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue

data class TopUpLimitValues(val minValue: FiatValue,
                            val maxValue: FiatValue)