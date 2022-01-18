package com.asfoundation.wallet.ui.backup.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase

class BackupCreationViewModelFactory(
    private val data: BackupCreationData,
    private val sendBackupToEmailUseCase: SendBackupToEmailUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val logger: Logger
) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return BackupCreationViewModel(data, sendBackupToEmailUseCase, backupSuccessLogUseCase,
        logger) as T
  }
}