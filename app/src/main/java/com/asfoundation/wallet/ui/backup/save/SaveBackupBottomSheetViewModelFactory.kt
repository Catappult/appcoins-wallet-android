package com.asfoundation.wallet.ui.backup.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.backup.save.use_cases.SaveBackupFileUseCase
import java.io.File

class SaveBackupBottomSheetViewModelFactory(
    private val data: SaveBackupBottomSheetData,
    private val saveBackupFileUseCase: SaveBackupFileUseCase,
    private val downloadsPath: File?) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return SaveBackupBottomSheetViewModel(data, saveBackupFileUseCase, downloadsPath) as T
  }
}