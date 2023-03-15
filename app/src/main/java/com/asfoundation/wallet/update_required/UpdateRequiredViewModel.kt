package com.asfoundation.wallet.update_required

import android.content.Intent
import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.wallets.usecases.ObserveWalletsModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class UpdateRequiredSideEffect : com.appcoins.wallet.ui.arch.SideEffect {
  data class UpdateActionIntent(val intent: Intent) : UpdateRequiredSideEffect()
  data class NavigateToBackup(val walletAddress: String) : UpdateRequiredSideEffect()
  data class ShowBackupOption(val walletsModel: WalletsModel) : UpdateRequiredSideEffect()
}

data class UpdateRequiredState(val walletsModel: com.appcoins.wallet.ui.arch.Async<WalletsModel> = com.appcoins.wallet.ui.arch.Async.Uninitialized) :
  com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class UpdateRequiredViewModel @Inject constructor(
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val observeWalletsModelUseCase: ObserveWalletsModelUseCase
) : com.appcoins.wallet.ui.arch.BaseViewModel<UpdateRequiredState, UpdateRequiredSideEffect>(initialState()) {

  companion object {
    fun initialState(): UpdateRequiredState {
      return UpdateRequiredState()
    }
  }

  fun checkBackupOption() {
    observeWalletsModelUseCase()
      .asAsyncToState {
        copy(walletsModel = it)
      }
      .scopedSubscribe()
  }

  fun handleUpdateClick() {
    sendSideEffect { UpdateRequiredSideEffect.UpdateActionIntent(buildUpdateIntentUseCase()) }
  }

  fun handleBackupClick(walletAddress: String) {
    sendSideEffect { UpdateRequiredSideEffect.NavigateToBackup(walletAddress) }
  }
}