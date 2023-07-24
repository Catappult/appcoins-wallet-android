package com.asfoundation.wallet.eskills.withdraw


sealed class WithdrawAmountResult

data class SuccessfulWithdrawAmount(val amount: String) : WithdrawAmountResult()

sealed class FailedWithdrawAmount : WithdrawAmountResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedWithdrawAmount()
  data class NoBalanceCode(val throwable: Throwable? = null) : FailedWithdrawAmount()
}

//class WithdrawAmountMapper {
//  fun map(withdrawAmount: WithdrawAmount): WithdrawAmountResult {
//    return when (withdrawAmount.validity) {
//      ValidityState.ACTIVE -> SuccessfulWithdrawAmount(withdrawAmount)
//      ValidityState.EXPIRED -> FailedWithdrawAmount.ExpiredCode()
//      ValidityState.ERROR -> FailedWithdrawAmount.InvalidCode()
//      ValidityState.NOT_ADDED -> FailedWithdrawAmount.CodeNotAdded()
//      else -> FailedWithdrawAmount.CodeNotAdded()
//    }
//  }
//}
