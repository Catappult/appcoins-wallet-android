package com.appcoins.wallet.feature.backup.data.use_cases

import androidx.documentfile.provider.DocumentFile
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveBackupFileUseCase @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val backupRepository: BackupRepository,
    private val dispatchers: Dispatchers
) {

  suspend operator fun invoke(
    walletAddress: String,
    password: String,
    fileName: String,
    filePath: DocumentFile?
  ) {
    val backupData = createBackupUseCase(walletAddress, password)
    withContext(dispatchers.io) {
      backupRepository.saveFile(backupData, filePath, fileName)
    }
  }
}