package com.asfoundation.wallet.backup.save_on_device

import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SaveBackupFileUseCase
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import java.io.File

sealed class BackupSaveOnDeviceDialogSideEffect : SideEffect {
  object NavigateToSuccess : BackupSaveOnDeviceDialogSideEffect()
  object ShowError : BackupSaveOnDeviceDialogSideEffect()
}

data class BackupSaveOnDeviceDialogState(val fileName: String, val downloadsPath: String?) :
  ViewState

class BackupSaveOnDeviceDialogViewModel(
  private val data: BackupSaveOnDeviceDialogData,
  private val saveBackupFileUseCase: SaveBackupFileUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val downloadsPath: File?
) :
  BaseViewModel<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceDialogSideEffect>(
    initialState(data, downloadsPath)
  ) {

  companion object {
    fun initialState(
      data: BackupSaveOnDeviceDialogData,
      downloadsPath: File?
    ): BackupSaveOnDeviceDialogState {
      return BackupSaveOnDeviceDialogState(
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
      .doOnComplete { sendSideEffect { BackupSaveOnDeviceDialogSideEffect.NavigateToSuccess } }
        .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { BackupSaveOnDeviceDialogSideEffect.ShowError }
  }
}