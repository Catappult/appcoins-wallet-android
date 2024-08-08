package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.base.compat.PostUserEmailUseCase
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.backup.data.result.BackupResult
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase
import com.appcoins.wallet.sharedpreferences.EmailPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BackupSaveOptionsSideEffect : SideEffect {
  object NavigateToSuccess : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

data class BackupSaveOptionsState(var saveOptionAsync: Async<BackupResult> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class BackupSaveOptionsViewModel
@Inject
constructor(
  private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val postUserEmailUseCase: PostUserEmailUseCase,
  private val logger: Logger,
) : BaseViewModel<BackupSaveOptionsState, BackupSaveOptionsSideEffect>(initialState()) {

  lateinit var walletAddress: String
  var password: String = ""
  val showLoading: MutableState<Boolean> = mutableStateOf(false)

  companion object {
    private val TAG = BackupSaveOptionsViewModel::class.java.name

    fun initialState(): BackupSaveOptionsState {
      return BackupSaveOptionsState()
    }
  }

  fun sendBackupToEmail(email: String) {
    sendBackupToEmailUseCase(walletAddress, password, email)
      .andThen(backupSuccessLogUseCase(walletAddress))
      .doOnComplete {
        sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess }
      }
      .doOnError { showError(it) }
      .subscribe()
  }

  fun postUserEmailCommunication(email: String) {
    postUserEmailUseCase(email).doOnError {
      it.printStackTrace()
    }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }

  fun showLoading(show: Boolean = true) {
    showLoading.value = show
  }
}
