package com.asfoundation.wallet.recover.use_cases

import android.net.Uri
import android.os.Build
import com.asfoundation.wallet.backup.FileInteractor
import repository.BackupRestorePreferencesRepository
import javax.inject.Inject

class GetFilePathUseCase @Inject constructor(
  private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
  private val fileInteractor: FileInteractor
) {
  operator fun invoke(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      backupRestorePreferencesRepository.getChosenUri()
        ?.let { Uri.parse(it) }
    } else {
      fileInteractor.getDownloadPath()
        ?.let { fileInteractor.getUriFromFile(it) }
    }
  }
}