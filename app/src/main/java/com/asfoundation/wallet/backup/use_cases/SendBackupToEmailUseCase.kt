package com.asfoundation.wallet.backup.use_cases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.backup.repository.BackupRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendBackupToEmailUseCase @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val backupRepository: BackupRepository,
    private val dispatchers: Dispatchers,
) {

  suspend operator fun invoke(
      walletAddress: String,
      password: String,
      email: String
  ): Unit {
    val backupData = createBackupUseCase(walletAddress, password)
    withContext(dispatchers.io) {
      backupRepository.sendBackupEmail(walletAddress, backupData, email)
    }
  }
}