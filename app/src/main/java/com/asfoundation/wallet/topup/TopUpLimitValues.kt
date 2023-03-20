package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.Error
import java.math.BigDecimal

data class TopUpLimitValues(val minValue: FiatValue = INITIAL_LIMIT_VALUE,
                            val maxValue: FiatValue = INITIAL_LIMIT_VALUE,
                            val error: Error = Error()
) {

  constructor(isNoNetwork: Boolean) : this(error = Error(
    true,
    isNoNetwork
  )
  )

  companion object {
    val INITIAL_LIMIT_VALUE = FiatValue(BigDecimal(-1), "", "")
  }
}