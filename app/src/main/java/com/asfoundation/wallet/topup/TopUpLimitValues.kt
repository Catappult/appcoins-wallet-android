package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal

data class TopUpLimitValues(val minValue: FiatValue = INITIAL_LIMIT_VALUE,
                            val maxValue: FiatValue = INITIAL_LIMIT_VALUE) {
  companion object {
    val INITIAL_LIMIT_VALUE = FiatValue(BigDecimal(-1), "", "")
  }
}