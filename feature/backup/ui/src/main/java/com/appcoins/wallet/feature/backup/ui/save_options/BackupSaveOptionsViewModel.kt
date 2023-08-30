package com.appcoins.wallet.feature.backup.ui.save_options

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.jvm_common.Logger

sealed class BackupSaveOptionsSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

object BackupSaveOptionsState : ViewState


class BackupSaveOptionsViewModel(
  private val data: BackupSaveOptionsData,
  private val sendBackupToEmailUseCase: com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase,
  private val logger: Logger,
) : BaseViewModel<BackupSaveOptionsState, BackupSaveOptionsSideEffect>(
  initialState()
) {

  companion object {
    private val TAG = BackupSaveOptionsViewModel::class.java.name

    fun initialState(): BackupSaveOptionsState {
      return BackupSaveOptionsState
    }
  }

  suspend fun sendBackupToEmail(text: String) {
    runCatching {
      sendBackupToEmailUseCase(data.walletAddress, data.password, text)
    }.onSuccess {
      backupSuccessLogUseCase(data.walletAddress)
      sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess(data.walletAddress) }
    }.onFailure {
      showError(it)
    }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}