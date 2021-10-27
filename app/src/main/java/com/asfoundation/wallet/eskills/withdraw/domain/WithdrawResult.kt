package com.asfoundation.wallet.eskills.withdraw.domain

import java.math.BigDecimal

sealed class WithdrawResult

data class SuccessfulWithdraw(val amount: BigDecimal) : WithdrawResult()

sealed class FailedWithdraw : WithdrawResult() {
  data class GenericError(val detail: String) : FailedWithdraw()
  data class NotEnoughEarningError(val detail: String) : FailedWithdraw()
  data class NotEnoughBalanceError(val detail: String) : FailedWithdraw()
  data class MinAmountRequiredError(val detail: String, val amount: BigDecimal) : FailedWithdraw()
  object NoNetworkError : FailedWithdraw()
  object InvalidEmailError : FailedWithdraw()
}
