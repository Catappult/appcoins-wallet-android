package com.appcoins.wallet.feature.backup.ui.save_on_device

import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SaveBackupFileUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class BackupSaveOnDeviceSideEffect : SideEffect {
  object NavigateToSuccess : BackupSaveOnDeviceSideEffect()
  object ShowError : BackupSaveOnDeviceSideEffect()
}

data class BackupSaveOnDeviceDialogState(
  val fileName: Async<String> = Async.Uninitialized,
  val walletAddress: String,
  val walletPassword: String,
  val downloadsPath: String?,
) : ViewState

@HiltViewModel
class BackupSaveOnDeviceViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val saveBackupFileUseCase: SaveBackupFileUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  walletInfoUseCase: GetWalletInfoUseCase,
  dispatchers: Dispatchers
) :
  NewBaseViewModel<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceSideEffect>(
    initialState(savedStateHandle)
  ) {

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"

    private val downloadsPath =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    fun initialState(savedStateHandle: SavedStateHandle) =
      savedStateHandle.run {
        val address = get<String>(WALLET_ADDRESS_KEY)!!
        val password = get<String>(PASSWORD_KEY)!!
        BackupSaveOnDeviceDialogState(
          Async.Loading("walletbackup${address}"),
          address,
          password,
          downloadsPath?.absolutePath
        )
      }
  }

  init {
    viewModelScope.launch {
      withContext(dispatchers.io) {
        val walletInfo =
          walletInfoUseCase(state.walletAddress, cached = true).await()
        suspend { walletInfo.name }
          .mapSuspendToAsync((BackupSaveOnDeviceDialogState::fileName)) { copy(fileName = it) }
      }
    }
  }

  fun saveBackupFile(
    fileName: String,
    filePath: DocumentFile? = downloadsPath?.let { DocumentFile.fromFile(it) }
  ) {
    saveBackupFileUseCase(state.walletAddress, state.walletPassword, fileName, filePath)
      .andThen(Completable.defer { backupSuccessLogUseCase(state.walletAddress) })
      .doOnComplete { sendSideEffect { BackupSaveOnDeviceSideEffect.NavigateToSuccess } }
      .doOnError { showError(it) }
      .subscribe()
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { BackupSaveOnDeviceSideEffect.ShowError }
  }
}

