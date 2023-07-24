package com.asfoundation.wallet.eskills.withdraw

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject

sealed class WithdrawBottomSheetSideEffect : SideEffect {
  object NavigateBack : WithdrawBottomSheetSideEffect()
}

data class WithdrawBottomSheetState(
  val withdrawAmountAsync: Async<BigDecimal> = Async.Uninitialized,
  val submitWithdrawAsync: Async<WithdrawAmountResult> = Async.Uninitialized, // TODO
) : ViewState

@HiltViewModel
class WithdrawBottomSheetViewModel @Inject constructor(
  private val getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
  private val getStoredUserEmailUseCase: GetStoredUserEmailUseCase,
  private val withdrawToFiatUseCase: WithdrawToFiatUseCase
) :
  BaseViewModel<WithdrawBottomSheetState, WithdrawBottomSheetSideEffect>(initialState()) {

  // to prevent success being called multiple times when any async is changed
  var isFirstSuccess = true

  companion object {
    fun initialState(): WithdrawBottomSheetState {
      return WithdrawBottomSheetState()
    }
  }

  init {
    getCurrentWithdrawBalance()
  }

  private fun getCurrentWithdrawBalance() {  //TODO
    getAvailableAmountToWithdrawUseCase()
      .asAsyncToState { copy(withdrawAmountAsync = it) }
      .repeatableScopedSubscribe(WithdrawBottomSheetState::withdrawAmountAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun submitClick(email: String, amount: String) {
//    verifyAndWithdrawUseCase(email, amount)   //TODO
//      .asAsyncToState { copy(submitWithdrawAsync = it) }
//      .scopedSubscribe()
  }

}