package com.asfoundation.wallet.ui.backup.creation

import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository

class BackupCreationInteractor(
    private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository) {

  fun saveBackedUpOnce() {
    backupRestorePreferencesRepository.saveBackedUpOnce()
  }
}
