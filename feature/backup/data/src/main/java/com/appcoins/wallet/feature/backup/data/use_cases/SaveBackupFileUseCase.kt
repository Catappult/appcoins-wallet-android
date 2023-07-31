package com.appcoins.wallet.feature.backup.data.use_cases

import android.annotation.SuppressLint
import androidx.documentfile.provider.DocumentFile
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import com.appcoins.wallet.feature.backup.data.result.BackupResult
import javax.inject.Inject

class SaveBackupFileUseCase @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val backupRepository: BackupRepository,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val dispatchers: Dispatchers
) {

  @SuppressLint("SuspiciousIndentation")
  suspend operator fun invoke(
      walletAddress: String,
      password: String,
      fileName: String,
      filePath: DocumentFile?
  ): BackupResult {
    backupSuccessLogUseCase(walletAddress).let {  }
    val backupData = createBackupUseCase(walletAddress, password)
      return backupRepository.saveFile(backupData, filePath, fileName)
    }
}