package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import kotlinx.coroutines.rx2.await
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
  ) {
    val backupData = createBackupUseCase(walletAddress, password)
    withContext(dispatchers.io) {
      backupRepository.sendBackupEmail(walletAddress, backupData, email).await()
    }
  }
}