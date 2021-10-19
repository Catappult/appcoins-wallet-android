package com.asfoundation.wallet.eskills_withdraw

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.WithdrawToFiatUseCase
import java.math.BigDecimal

object WithdrawSideEffect : SideEffect

data class WithdrawState(
    val availableAmountAsync: Async<BigDecimal> = Async.Uninitialized,
    val withdrawResultAsync: Async<WithdrawResult> = Async.Uninitialized,
    val userEmail: String = ""
) : ViewState

class WithdrawViewModel(
    private val getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
    private val getStoredUserEmailUseCase: GetStoredUserEmailUseCase,
    private val withdrawToFiatUseCase: WithdrawToFiatUseCase
) : BaseViewModel<WithdrawState, WithdrawSideEffect>(initialState()) {

  companion object {
    fun initialState(): WithdrawState {
      return WithdrawState()
    }
  }

  init {
    setAvailableAmount()
    setStoredUserEmail()
  }

  private fun setAvailableAmount() {
    getAvailableAmountToWithdrawUseCase()
        .asAsyncToState { copy(availableAmountAsync = it) }
        .repeatableScopedSubscribe(WithdrawState::availableAmountAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun setStoredUserEmail() {
    getStoredUserEmailUseCase()
        .doOnSuccess { setState { copy(userEmail = it) } }
        .scopedSubscribe { }
  }

  fun withdrawToFiat(paypalEmail: String, amount: BigDecimal) {
    withdrawToFiatUseCase(paypalEmail, amount)
        .asAsyncToState(WithdrawState::withdrawResultAsync) {
          copy(withdrawResultAsync = it)
        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }
}
