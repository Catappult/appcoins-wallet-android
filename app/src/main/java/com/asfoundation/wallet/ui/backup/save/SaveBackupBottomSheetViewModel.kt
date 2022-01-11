package com.asfoundation.wallet.ui.backup.save

import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SaveBackupFileUseCase
import java.io.File

sealed class SaveBackupBottomSheetSideEffect : SideEffect {
  object NavigateToSuccess : SaveBackupBottomSheetSideEffect()
  object ShowError : SaveBackupBottomSheetSideEffect()
}

data class SaveBackupBottomSheetState(val fileName: String, val downloadsPath: String?) : ViewState

class SaveBackupBottomSheetViewModel(
    private val data: SaveBackupBottomSheetData,
    private val saveBackupFileUseCase: SaveBackupFileUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val downloadsPath: File?) :
    BaseViewModel<SaveBackupBottomSheetState, SaveBackupBottomSheetSideEffect>(
        initialState(data, downloadsPath)) {

  companion object {
    fun initialState(data: SaveBackupBottomSheetData,
                     downloadsPath: File?): SaveBackupBottomSheetState {
      return SaveBackupBottomSheetState("walletbackup${data.walletAddress}",
          downloadsPath?.absolutePath)
    }
  }

  fun saveBackupFile(fileName: String, filePath: DocumentFile? = downloadsPath?.let {
    DocumentFile.fromFile(it)
  }) {
    saveBackupFileUseCase(data.walletAddress, data.password, fileName, filePath)
        .doOnComplete { backupSuccessLogUseCase(data.walletAddress) }
        .doOnComplete { sendSideEffect { SaveBackupBottomSheetSideEffect.NavigateToSuccess } }
        .repeatableScopedSubscribe("saveBackupFile") { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    sendSideEffect { SaveBackupBottomSheetSideEffect.ShowError }
  }
}