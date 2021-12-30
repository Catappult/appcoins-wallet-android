package com.asfoundation.wallet.ui.backup.use_cases

import com.asfoundation.wallet.ui.backup.repository.BackupRepository
import io.reactivex.Completable

class SendBackupToEmailUseCase(private val createBackupUseCase: CreateBackupUseCase,
                               private val backupRepository: BackupRepository) {

  operator fun invoke(walletAddress: String,
                      password: String,
                      email: String): Completable {
    return createBackupUseCase(walletAddress, password)
        .flatMapCompletable { backupRepository.sendBackupEmail(it, email) }
  }
}