package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.backup.repository.BackupRepository
import io.reactivex.Completable
import javax.inject.Inject

class SendBackupToEmailUseCase @Inject constructor(
  private val createBackupUseCase: CreateBackupUseCase,
  private val backupRepository: BackupRepository
) {

  operator fun invoke(
    walletAddress: String,
    password: String,
    email: String
  ): Completable {
    return createBackupUseCase(walletAddress, password)
      .flatMapCompletable { backupRepository.sendBackupEmail(walletAddress, it, email) }
  }
}