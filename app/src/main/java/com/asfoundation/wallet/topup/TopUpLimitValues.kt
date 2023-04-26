package com.asfoundation.wallet.topup

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.Error
import java.math.BigDecimal

data class TopUpLimitValues(val minValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue = INITIAL_LIMIT_VALUE,
                            val maxValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue = INITIAL_LIMIT_VALUE,
                            val error: Error = Error()
) {

  constructor(isNoNetwork: Boolean) : this(error = Error(
    true,
    isNoNetwork
  )
  )

  companion object {
    val INITIAL_LIMIT_VALUE =
      com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue(BigDecimal(-1), "", "")
  }
}