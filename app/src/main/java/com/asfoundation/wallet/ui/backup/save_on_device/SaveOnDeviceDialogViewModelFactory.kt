package com.asfoundation.wallet.ui.backup.save_on_device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SaveBackupFileUseCase
import java.io.File

class SaveOnDeviceDialogViewModelFactory(
    private val data: SaveOnDeviceDialogData,
    private val saveBackupFileUseCase: SaveBackupFileUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val downloadsPath: File?) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return SaveOnDeviceDialogViewModel(
      data, saveBackupFileUseCase, backupSuccessLogUseCase,
      downloadsPath
    ) as T
  }
}