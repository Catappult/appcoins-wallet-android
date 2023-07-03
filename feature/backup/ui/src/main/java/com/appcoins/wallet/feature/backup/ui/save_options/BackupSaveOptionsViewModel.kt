package com.appcoins.wallet.feature.backup.ui.save_options

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BackupSaveOptionsSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

object BackupSaveOptionsState : ViewState

@HiltViewModel
class BackupSaveOptionsViewModel @Inject constructor(
  //private val data: BackupSaveOptionsData,
  private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val logger: Logger,
) : BaseViewModel<BackupSaveOptionsState, BackupSaveOptionsSideEffect>(
    initialState()
) {
  lateinit var walletAddress: String
  lateinit var password: String

  companion object {
    private val TAG = BackupSaveOptionsViewModel::class.java.name

    fun initialState(): BackupSaveOptionsState {
      return BackupSaveOptionsState
    }
  }

  suspend fun sendBackupToEmail(text: String) {
    runCatching {
      sendBackupToEmailUseCase(walletAddress, password, text)
    }.onSuccess {
      backupSuccessLogUseCase(walletAddress).let {}
    }.onFailure {
      showError(it)
    }
    try {
      backupSuccessLogUseCase(walletAddress).let {}
      sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess(walletAddress) }
    } catch (e: Exception) {
      showError(e)
    }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}