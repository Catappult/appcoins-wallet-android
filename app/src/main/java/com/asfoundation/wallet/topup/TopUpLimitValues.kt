package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal

data class TopUpLimitValues(val minValue: FiatValue = initialLimitValue,
                            val maxValue: FiatValue = initialLimitValue) {
  companion object {
    private val initialLimitValue = FiatValue(BigDecimal(-1), "", "")
  }
}