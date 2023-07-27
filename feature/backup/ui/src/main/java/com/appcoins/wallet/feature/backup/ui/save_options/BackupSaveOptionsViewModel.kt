package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.data.Error
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.backup.data.result.BackupResult
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupSaveOptionsSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

data class BackupSaveOptionsState(var saveOptionAsync: Async<BackupResult> = Async.Uninitialized) : ViewState

@HiltViewModel
class BackupSaveOptionsViewModel @Inject constructor(
  private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val logger: Logger,
) : BaseViewModel<BackupSaveOptionsState, BackupSaveOptionsSideEffect>(
    initialState()
) {

  lateinit var walletAddress: String
  var password : String = ""

  companion object {
    private val TAG = BackupSaveOptionsViewModel::class.java.name

    fun initialState(): BackupSaveOptionsState {
      return BackupSaveOptionsState()
    }
  }

  fun sendBackupToEmail(text: String) {
    viewModelScope.launch {
      runCatching {
        sendBackupToEmailUseCase(walletAddress, password, text)
      }.onSuccess {
        backupSuccessLogUseCase(walletAddress).let {}
          setState {  copy(saveOptionAsync = Async.Success(it))}
      }.onFailure {
        setState {  copy(saveOptionAsync = Async.Fail(Error.UnknownError(Throwable(""))))}

      }
    }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}