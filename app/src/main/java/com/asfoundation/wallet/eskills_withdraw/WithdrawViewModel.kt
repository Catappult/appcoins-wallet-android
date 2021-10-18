package com.asfoundation.wallet.eskills_withdraw

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawAvailableAmount
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.WithdrawToFiatUseCase
import java.math.BigDecimal

object WithdrawSideEffect : SideEffect

data class WithdrawState(
    val availableAmount: Async<WithdrawAvailableAmount> = Async.Uninitialized,
    val withdrawResultAsync: Async<WithdrawResult> = Async.Uninitialized
) : ViewState

class WithdrawViewModel(
    private val getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
    private val withdrawToFiatUseCase: WithdrawToFiatUseCase
) : BaseViewModel<WithdrawState, WithdrawSideEffect>(initialState()) {

  companion object {
    fun initialState(): WithdrawState {
      return WithdrawState()
    }
  }

  init {
    setAvailableAmount()
  }

  private fun setAvailableAmount() {
    getAvailableAmountToWithdrawUseCase()
        .asAsyncToState(WithdrawState::availableAmount) {
          copy(availableAmount = it)
        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  fun withdrawToFiat(paypalEmail: String, amount: BigDecimal) {
    withdrawToFiatUseCase(paypalEmail, amount)
        .asAsyncToState(WithdrawState::withdrawResultAsync) {
          copy(withdrawResultAsync = it)
        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }
}
