package com.asfoundation.wallet.update_required

import android.content.Intent
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class UpdateRequiredSideEffect : SideEffect {
  data class UpdateActionIntent(val intent: Intent) : UpdateRequiredSideEffect()
}

object UpdateRequiredState : ViewState

@HiltViewModel
class UpdateRequiredViewModel @Inject constructor(
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase
) : BaseViewModel<UpdateRequiredState, UpdateRequiredSideEffect>(initialState()) {

  companion object {
    fun initialState(): UpdateRequiredState {
      return UpdateRequiredState
    }
  }

  fun handleUpdateClick() {
    sendSideEffect { UpdateRequiredSideEffect.UpdateActionIntent(buildUpdateIntentUseCase()) }
  }
}