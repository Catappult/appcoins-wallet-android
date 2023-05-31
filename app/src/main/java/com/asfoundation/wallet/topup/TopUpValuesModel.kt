package com.asfoundation.wallet.topup

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.Error

data class TopUpValuesModel(val values: List<FiatValue>, val error: Error = Error()) {
  constructor(isNoNetworkError: Boolean) : this(listOf(FiatValue()),
    Error(true, isNoNetworkError)
  )
}
