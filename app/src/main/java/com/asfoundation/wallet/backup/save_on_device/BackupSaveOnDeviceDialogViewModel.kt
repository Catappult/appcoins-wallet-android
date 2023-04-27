package com.asfoundation.wallet.backup.save_on_device

import android.os.Build
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.arch.*
import com.appcoins.wallet.core.arch.data.Async
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SaveBackupFileUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
  rxSchedulers: RxSchedulers
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
    walletInfoUseCase(state.walletAddress, cached = true, updateFiat = false)
      .map { it.name }
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(BackupSaveOnDeviceDialogState::fileName) { copy(fileName = it) }
      .scopedSubscribe(Throwable::printStackTrace)
  }

  fun saveBackupFile(
    fileName: String,
    filePath: DocumentFile? = downloadsPath?.let { DocumentFile.fromFile(it) }
  ) {
    saveBackupFileUseCase(state.walletAddress, state.walletPassword, fileName, filePath)
      .andThen(backupSuccessLogUseCase(state.walletAddress))
      .doOnComplete { sendSideEffect { BackupSaveOnDeviceDialogSideEffect.NavigateToSuccess(state.walletAddress) } }
      .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { BackupSaveOnDeviceDialogSideEffect.ShowError }
  }
}
