package com.asfoundation.wallet.interact

import com.asfoundation.wallet.fingerprint.FingerprintPreferenceRepositoryContract
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
                           private val fingerprintPreferences: FingerprintPreferenceRepositoryContract) {

  fun delete(address: String): Completable {
    return passwordStore.getPassword(address)
        .flatMapCompletable { walletRepository.deleteWallet(address, it) }
        .andThen(preferencesRepositoryType.removeWalletValidationStatus(address))
        .andThen(preferencesRepositoryType.removeWalletRestoreBackup(address))
        .andThen(preferencesRepositoryType.removeBackupNotificationSeenTime(address))
  }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}