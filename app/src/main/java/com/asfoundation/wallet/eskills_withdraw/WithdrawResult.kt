package com.asfoundation.wallet.eskills_withdraw

import java.math.BigDecimal

data class WithdrawResult(val amount: BigDecimal, val status: Status) {
  enum class Status {
    SUCCESS, NOT_ENOUGH_EARNING, NOT_ENOUGH_BALANCE, NO_NETWORK, INVALID_EMAIL,
    MIN_AMOUNT_REQUIRED, ERROR
  }
}
