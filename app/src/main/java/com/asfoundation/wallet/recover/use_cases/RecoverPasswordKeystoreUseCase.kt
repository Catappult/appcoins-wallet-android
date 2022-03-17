package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.recover.result.*
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class RecoverPasswordKeystoreUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository
) {

  operator fun invoke(keystore: String, password: String = ""): Single<RecoverPasswordResult> {
    return passwordStore.generatePassword()
      .flatMap { newPassword ->
        walletRepository.restoreKeystoreToWallet(keystore, password, newPassword)
      }
      .flatMap {
        RecoverPasswordResultMapper(keystore).map(it)
      }
      .doOnSuccess {
        when (it) {
          is SuccessfulPasswordRecover -> backupRestorePreferencesRepository.setWalletRestoreBackup(
            it.address
          )
          else -> Unit
        }
      }

  }
}