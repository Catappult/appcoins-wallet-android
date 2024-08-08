package com.asfoundation.wallet.eskills.withdraw.domain

import java.math.BigDecimal

sealed class WithdrawResult

data class SuccessfulWithdraw(val amount: BigDecimal) : WithdrawResult()

sealed class FailedWithdraw : WithdrawResult() {
  object GenericError : FailedWithdraw()
  object NotEnoughEarningError : FailedWithdraw()
  object NotEnoughBalanceError : FailedWithdraw()
  data class MinAmountRequiredError(val amount: BigDecimal) : FailedWithdraw()
  object NoNetworkError : FailedWithdraw()
  object InvalidEmailError : FailedWithdraw()
}
