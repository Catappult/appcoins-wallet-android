package com.asfoundation.wallet.backup.save_options

import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SendBackupToEmailUseCase
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState

sealed class BackupSaveOptionsSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

object BackupSaveOptionsState : ViewState


class BackupSaveOptionsViewModel(
  private val data: BackupSaveOptionsData,
  private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
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

  fun sendBackupToEmail(text: String) {
    sendBackupToEmailUseCase(data.walletAddress, data.password, text)
      .andThen(backupSuccessLogUseCase(data.walletAddress))
      .doOnComplete { sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess(data.walletAddress) } }
      .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}