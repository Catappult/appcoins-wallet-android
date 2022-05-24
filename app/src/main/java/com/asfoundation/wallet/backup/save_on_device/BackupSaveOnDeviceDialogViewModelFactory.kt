package com.asfoundation.wallet.backup.save_on_device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SaveBackupFileUseCase
import java.io.File

class BackupSaveOnDeviceDialogViewModelFactory(
  private val data: BackupSaveOnDeviceDialogData,
  private val saveBackupFileUseCase: SaveBackupFileUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
  private val downloadsPath: File?
) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupSaveOnDeviceDialogViewModel(
      data, saveBackupFileUseCase, backupSuccessLogUseCase,
      downloadsPath
    ) as T
  }
}