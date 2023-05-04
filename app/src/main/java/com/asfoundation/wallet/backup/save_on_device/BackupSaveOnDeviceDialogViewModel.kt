package com.asfoundation.wallet.backup.save_on_device

import android.os.Build
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SaveBackupFileUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class BackupSaveOnDeviceDialogSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOnDeviceDialogSideEffect()
  object ShowError : BackupSaveOnDeviceDialogSideEffect()
}

data class BackupSaveOnDeviceDialogState(
    val fileName: Async<String> = Async.Uninitialized,
    val walletAddress: String,
    val walletPassword: String,
    val downloadsPath: String?,
) : ViewState

@HiltViewModel
class BackupSaveOnDeviceDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveBackupFileUseCase: SaveBackupFileUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    walletInfoUseCase: GetWalletInfoUseCase,
    dispatchers: Dispatchers
) :
    BaseViewModel<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceDialogSideEffect>(
        initialState(savedStateHandle)
    ) {

  companion object {
    private val downloadsPath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      @Suppress("DEPRECATION")
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    } else {
      null
    }

    fun initialState(savedStateHandle: SavedStateHandle) =
        savedStateHandle.run {
          val address = get<String>(BackupSaveOnDeviceDialogFragment.WALLET_ADDRESS_KEY)!!
          val password = get<String>(BackupSaveOnDeviceDialogFragment.PASSWORD_KEY)!!
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
            walletInfoUseCase(state.walletAddress, cached = true, updateFiat = false).await()
        suspend { walletInfo.name }.mapAsyncToState((BackupSaveOnDeviceDialogState::fileName)) {
          copy(
              fileName = it
          )
        }
      }
    }
  }

  fun saveBackupFile(
      fileName: String,
      filePath: DocumentFile? = downloadsPath?.let { DocumentFile.fromFile(it) }
  ) {
    try {
      viewModelScope.launch {
        saveBackupFileUseCase(state.walletAddress, state.walletPassword, fileName, filePath)
        backupSuccessLogUseCase(state.walletAddress)
        sendSideEffect { BackupSaveOnDeviceDialogSideEffect.NavigateToSuccess(state.walletAddress) }
      }
    } catch (e: Exception) {
      showError(e)
    }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { BackupSaveOnDeviceDialogSideEffect.ShowError }
  }
}
