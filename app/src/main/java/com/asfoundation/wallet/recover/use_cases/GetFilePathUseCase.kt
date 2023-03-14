package com.asfoundation.wallet.recover.use_cases

import android.net.Uri
import android.os.Build
import com.asfoundation.wallet.backup.FileInteractor
import repository.BackupRestorePreferencesDataSource
import javax.inject.Inject

class GetFilePathUseCase @Inject constructor(
  private val backupRestorePreferencesDataSource: BackupRestorePreferencesDataSource,
  private val fileInteractor: FileInteractor
) {
  operator fun invoke(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      backupRestorePreferencesDataSource.getChosenUri()
        ?.let { Uri.parse(it) }
    } else {
      fileInteractor.getDownloadPath()
        ?.let { fileInteractor.getUriFromFile(it) }
    }
  }
}