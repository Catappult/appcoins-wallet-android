package com.asfoundation.wallet.update_required

import android.content.Intent
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class UpdateRequiredSideEffect : SideEffect {
  data class UpdateActionIntent(val intent: Intent) : UpdateRequiredSideEffect()
  data class NavigateToBackup(val walletAddress: String) : UpdateRequiredSideEffect()
  data class ShowBackupOption(val shouldShow: Boolean) : UpdateRequiredSideEffect()
}

object UpdateRequiredState : ViewState

@HiltViewModel
class UpdateRequiredViewModel @Inject constructor(
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase
) : BaseViewModel<UpdateRequiredState, UpdateRequiredSideEffect>(initialState()) {

  companion object {
    fun initialState(): UpdateRequiredState {
      return UpdateRequiredState
    }
  }

  init {
    checkBackupOption()
  }

  private fun checkBackupOption() {
    getCurrentWalletUseCase()
      .doOnSuccess {
        sendSideEffect { UpdateRequiredSideEffect.ShowBackupOption(shouldShow = true) }
      }
      .doOnError {
        sendSideEffect { UpdateRequiredSideEffect.ShowBackupOption(shouldShow = false) }
      }
      .scopedSubscribe()
  }

  fun handleUpdateClick() {
    sendSideEffect { UpdateRequiredSideEffect.UpdateActionIntent(buildUpdateIntentUseCase()) }
  }

  fun handleBackupClick() {
    getCurrentWalletUseCase()
      .doOnSuccess {
        sendSideEffect { UpdateRequiredSideEffect.NavigateToBackup(it.address) }
      }
      .scopedSubscribe()
  }

}