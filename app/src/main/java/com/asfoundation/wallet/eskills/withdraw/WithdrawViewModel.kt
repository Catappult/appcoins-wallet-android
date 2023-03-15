package com.asfoundation.wallet.eskills.withdraw

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject

object WithdrawSideEffect : com.appcoins.wallet.ui.arch.SideEffect

data class WithdrawState(
  val availableAmountAsync: com.appcoins.wallet.ui.arch.Async<BigDecimal> = com.appcoins.wallet.ui.arch.Async.Uninitialized,
  val withdrawResultAsync: com.appcoins.wallet.ui.arch.Async<WithdrawResult> = com.appcoins.wallet.ui.arch.Async.Uninitialized,
  val userEmail: String = ""
) : com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class WithdrawViewModel @Inject constructor(
  private val getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
  private val getStoredUserEmailUseCase: GetStoredUserEmailUseCase,
  private val withdrawToFiatUseCase: WithdrawToFiatUseCase
) : com.appcoins.wallet.ui.arch.BaseViewModel<WithdrawState, WithdrawSideEffect>(initialState()) {

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
