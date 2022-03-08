package com.asfoundation.wallet.ui.backup.save_on_device

import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SaveBackupFileUseCase
import java.io.File

sealed class SaveOnDeviceDialogSideEffect : SideEffect {
  object NavigateToSuccess : SaveOnDeviceDialogSideEffect()
  object ShowError : SaveOnDeviceDialogSideEffect()
}

data class SaveOnDeviceDialogState(val fileName: String, val downloadsPath: String?) : ViewState

class SaveOnDeviceDialogViewModel(
  private val data: SaveOnDeviceDialogData,
  private val saveBackupFileUseCase: SaveBackupFileUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val downloadsPath: File?
) :
  BaseViewModel<SaveOnDeviceDialogState, SaveOnDeviceDialogSideEffect>(
    initialState(data, downloadsPath)
  ) {

  companion object {
    fun initialState(
      data: SaveOnDeviceDialogData,
      downloadsPath: File?
    ): SaveOnDeviceDialogState {
      return SaveOnDeviceDialogState(
        "walletbackup${data.walletAddress}",
        downloadsPath?.absolutePath
      )
    }
  }

  fun saveBackupFile(fileName: String, filePath: DocumentFile? = downloadsPath?.let {
    DocumentFile.fromFile(it)
  }) {
    saveBackupFileUseCase(data.walletAddress, data.password, fileName, filePath)
      .andThen(backupSuccessLogUseCase(data.walletAddress))
      .doOnComplete { sendSideEffect { SaveOnDeviceDialogSideEffect.NavigateToSuccess } }
        .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { SaveOnDeviceDialogSideEffect.ShowError }
  }
}