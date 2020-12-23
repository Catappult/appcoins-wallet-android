package com.asfoundation.wallet.interact

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Completable

/**
 * Delete and fetchTokens wallets
 */
class DeleteWalletInteract(private val walletRepository: WalletRepositoryType,
                           private val passwordStore: PasswordStore,
                           private val preferencesRepositoryType: PreferencesRepositoryType,
                           private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                           private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  fun delete(address: String): Completable {
    return passwordStore.getPassword(address)
        .flatMapCompletable { walletRepository.deleteWallet(address, it) }
        .andThen(preferencesRepositoryType.removeWalletValidationStatus(address))
        .andThen(backupRestorePreferencesRepository.removeWalletRestoreBackup(address))
        .andThen(backupRestorePreferencesRepository.removeBackupNotificationSeenTime(address))
  }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}