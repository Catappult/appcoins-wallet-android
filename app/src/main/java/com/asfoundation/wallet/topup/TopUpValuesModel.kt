package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.Error

data class TopUpValuesModel(val values: List<FiatValue>, val error: com.appcoins.wallet.core.utils.jvm_common.Error = com.appcoins.wallet.core.utils.jvm_common.Error()) {
  constructor(isNoNetworkError: Boolean) : this(listOf(FiatValue()),
    com.appcoins.wallet.core.utils.jvm_common.Error(true, isNoNetworkError)
  )
}
