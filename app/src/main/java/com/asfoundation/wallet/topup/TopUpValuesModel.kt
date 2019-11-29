package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.Error

data class TopUpValuesModel(val values: List<FiatValue>, val error: Error = Error()) {
  constructor(isNoNetworkError: Boolean) : this(listOf(FiatValue()), Error(true, isNoNetworkError))
}
