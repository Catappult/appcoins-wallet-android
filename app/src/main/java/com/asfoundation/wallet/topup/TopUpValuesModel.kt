package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue

data class TopUpValuesModel(val values: List<FiatValue>, val error: Boolean = false)
