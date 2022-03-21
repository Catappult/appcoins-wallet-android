package com.asfoundation.wallet.redeem_gift.bottom_sheet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class RedeemGiftBottomSheetSideEffect : SideEffect {
  object NavigateBack : RedeemGiftBottomSheetSideEffect()
}

data class RedeemGiftBottomSheetState(val redeemGiftAsync: Async<GiftCode> = Async.Uninitialized,
                                     val submitClickAsync: Async<Unit> = Async.Uninitialized,
                                     val shouldShowDefault: Boolean = false) : ViewState

@HiltViewModel
class RedeemGiftBottomSheetViewModel @Inject constructor(
  private val redeemGiftUseCase: RedeemGiftUseCase) :
  BaseViewModel<RedeemGiftBottomSheetState, RedeemGiftBottomSheetSideEffect>(initialState()) {

  companion object {
    fun initialState(): RedeemGiftBottomSheetState {
      return RedeemGiftBottomSheetState()
    }
  }

  fun submitClick(redeemGiftString: String) {
    redeemGiftUseCase(redeemGiftString)
      .asAsyncToState { copy(submitClickAsync = it) }
      .repeatableScopedSubscribe(RedeemGiftBottomSheetState::submitClickAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun replaceClick() = setState { copy(shouldShowDefault = true) }

  fun successGotItClick() = sendSideEffect { RedeemGiftBottomSheetSideEffect.NavigateBack }
}
