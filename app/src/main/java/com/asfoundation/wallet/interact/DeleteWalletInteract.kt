package com.asfoundation.wallet.interact

import preferences.BackupSystemNotificationPreferences
import preferences.BackupTriggerPreferences
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import fingerprint.FingerprintPreferencesDataSource
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
  private val fingerprintPreferences: FingerprintPreferencesDataSource,
  private val walletInfoRepository: WalletInfoRepository
) {

  fun delete(address: String): Completable = passwordStore.getPassword(address)
    .flatMapCompletable { walletRepository.deleteWallet(address, it) }
    .andThen(walletVerificationInteractor.removeWalletVerificationStatus(address))
    .andThen(
      Completable.fromAction {
        backupTriggerPreferences.removeBackupTriggerSeenTime(address)
      }
    )
    .andThen(
      Completable.fromAction {
        backupSystemNotificationPreferences.removeDismissedBackupSystemNotificationSeenTime(address)
      }
    )
    .andThen(walletInfoRepository.deleteWalletInfo(address))
    .andThen(setNewWallet())

  private fun setNewWallet(): Completable = walletRepository.fetchWallets()
    .filter(Array<Wallet>::isNotEmpty)
    .map { it[0] }
    .flatMapCompletable { walletRepository.setDefaultWallet(it.address) }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}