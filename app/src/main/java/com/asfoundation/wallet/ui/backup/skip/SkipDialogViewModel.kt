package com.asfoundation.wallet.ui.backup.skip

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState

sealed class SkipDialogSideEffect : SideEffect

object SkipDialogState : ViewState

class SkipDialogViewModel :
    BaseViewModel<SkipDialogState, SkipDialogSideEffect>(
        initialState()) {

  companion object {
    fun initialState(): SkipDialogState {
      return SkipDialogState
    }
  }
}