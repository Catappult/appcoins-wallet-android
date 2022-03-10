package com.asfoundation.wallet.ui.backup.save_options

import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase

sealed class BackupSaveOptionsSideEffect : SideEffect {
  object NavigateToSuccess : BackupSaveOptionsSideEffect()
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
      .doOnComplete { sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess } }
        .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}