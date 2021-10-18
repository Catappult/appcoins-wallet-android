package com.asfoundation.wallet.eskills_withdraw

data class WithdrawResult(val status: Status) {
  enum class Status {
    SUCCESS, NOT_ENOUGH_EARNING, NOT_ENOUGH_BALANCE, NO_NETWORK, INVALID_EMAIL
  }
}
