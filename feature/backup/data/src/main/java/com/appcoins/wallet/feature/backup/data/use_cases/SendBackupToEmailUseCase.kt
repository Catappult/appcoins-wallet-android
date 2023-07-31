package com.appcoins.wallet.feature.backup.data.use_cases

import android.annotation.SuppressLint
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import com.appcoins.wallet.feature.backup.data.result.BackupResult
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendBackupToEmailUseCase @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val backupRepository: BackupRepository,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase,
    private val dispatchers: Dispatchers,
) {

  @SuppressLint("SuspiciousIndentation")
  suspend operator fun invoke(
      walletAddress: String,
      password: String,
      email: String
  ): BackupResult {
    backupSuccessLogUseCase(walletAddress).let {  }
    val backupData = createBackupUseCase(walletAddress, password)
      return backupRepository.sendBackupEmail(walletAddress, backupData, email)

  }
}