package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase

class BackupSaveOptionsViewModelFactory(
    private val data: BackupSaveOptionsData,
    private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val logger: Logger,

    ) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupSaveOptionsViewModel(
        data, sendBackupToEmailUseCase, backupSuccessLogUseCase,
        logger
    ) as T
  }
}