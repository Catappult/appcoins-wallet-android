package com.appcoins.wallet.billing.common.response

import java.math.BigDecimal

data class TransactionFee(val type: Type, val cost: Cost?) {

  enum class Type {
    EXACT, UNKNOWN
  }

  data class Cost(val value: BigDecimal, val currency: String)
}



