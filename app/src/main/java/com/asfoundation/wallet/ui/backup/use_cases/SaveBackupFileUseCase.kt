package com.asfoundation.wallet.ui.backup.use_cases

import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.ui.backup.repository.BackupRepository
import io.reactivex.Completable

class SaveBackupFileUseCase(private val createBackupUseCase: CreateBackupUseCase,
                            private val backupRepository: BackupRepository) {

  operator fun invoke(walletAddress: String,
                      password: String,
                      fileName: String,
                      filePath: DocumentFile?): Completable {
    return createBackupUseCase(walletAddress, password)
        .flatMapCompletable { backupRepository.saveFile(it, filePath, fileName) }
  }
}