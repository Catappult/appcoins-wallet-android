package com.asfoundation.wallet.ui.backup.creation

import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase

sealed class BackupCreationSideEffect : SideEffect {
  object NavigateToSuccess : BackupCreationSideEffect()
  object ShowError : BackupCreationSideEffect()
}

object BackupCreationState : ViewState


class BackupCreationViewModel(
    private val data: BackupCreationData,
    private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val logger: Logger,
) : BaseViewModel<BackupCreationState, BackupCreationSideEffect>(
    initialState()) {

  companion object {
    private val TAG = BackupCreationViewModel::class.java.name

    fun initialState(): BackupCreationState {
      return BackupCreationState
    }
  }

  fun sendBackupToEmail(text: String) {
    sendBackupToEmailUseCase(data.walletAddress, data.password, text)
        .doOnComplete { backupSuccessLogUseCase(data.walletAddress) }
        .doOnComplete { sendSideEffect { BackupCreationSideEffect.NavigateToSuccess } }
        .repeatableScopedSubscribe("sendBackupToEmail") { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    logger.log(TAG, throwable)
    sendSideEffect { BackupCreationSideEffect.ShowError }
  }
}