package com.asfoundation.wallet.update_required

import android.content.Intent
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletsModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class UpdateRequiredSideEffect : SideEffect {
  data class UpdateActionIntent(val intent: Intent) : UpdateRequiredSideEffect()
  data class NavigateToBackup(val walletAddress: String) : UpdateRequiredSideEffect()
  data class ShowBackupOption(val walletsModel: WalletsModel) : UpdateRequiredSideEffect()
}

object UpdateRequiredState : ViewState

@HiltViewModel
class UpdateRequiredViewModel @Inject constructor(
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  private val getWalletsModelUseCase: GetWalletsModelUseCase
) : BaseViewModel<UpdateRequiredState, UpdateRequiredSideEffect>(initialState()) {

  companion object {
    fun initialState(): UpdateRequiredState {
      return UpdateRequiredState
    }
  }

  fun checkBackupOption() {
    getWalletsModelUseCase()
      .doOnSuccess {
        when (it.totalWallets) {
          0 -> Unit
          else -> {
            sendSideEffect { UpdateRequiredSideEffect.ShowBackupOption(it) }
          }
        }
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