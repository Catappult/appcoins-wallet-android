package com.asfoundation.wallet.interact

import com.asfoundation.wallet.backup.repository.preferences.BackupSystemNotificationPreferences
import com.asfoundation.wallet.backup.repository.preferences.BackupTriggerPreferences
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Delete and fetchTokens wallets
 */
class DeleteWalletInteract @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val backupTriggerPreferences: BackupTriggerPreferences,
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferences,
  private val fingerprintPreferences: FingerprintPreferencesRepositoryContract
) {

  fun delete(address: String): Completable {
    return passwordStore.getPassword(address)
      .flatMapCompletable { walletRepository.deleteWallet(address, it) }
      .andThen(walletVerificationInteractor.removeWalletVerificationStatus(address))
      .andThen(backupTriggerPreferences.removeBackupTriggerSeenTime(address))
      .andThen(backupSystemNotificationPreferences.removeDismissedBackupSystemNotificationSeenTime(address))
      .andThen(setNewWallet())
  }

  fun setNewWallet(): Completable {
    return walletRepository.fetchWallets()
      .filter { wallets -> wallets.isNotEmpty() }
      .map { wallets: Array<Wallet> ->
        wallets[0]
      }
      .flatMapCompletable { wallet: Wallet ->
        walletRepository.setDefaultWallet(wallet.address)
      }
  }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}