package com.asfoundation.wallet.ui.backup.save.use_cases

import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.ui.backup.save.repository.FileRepository
import io.reactivex.Completable

class SaveBackupFileUseCase(private val walletRepository: WalletRepositoryType,
                            private val passwordStore: PasswordStore,
                            private val fileRepository: FileRepository) {

  operator fun invoke(walletAddress: String,
                      password: String,
                      fileName: String,
                      filePath: DocumentFile?): Completable {
    return passwordStore.getPassword(walletAddress)
        .flatMap { walletRepository.exportWallet(walletAddress, it, password) }
        .flatMapCompletable { fileRepository.saveFile(it, filePath, fileName) }
  }
}