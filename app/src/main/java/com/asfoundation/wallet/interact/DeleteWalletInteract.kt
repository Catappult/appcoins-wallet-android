package com.asfoundation.wallet.interact

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.WalletInfoRepository
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import io.reactivex.Completable
import com.appcoins.wallet.sharedpreferences.BackupSystemNotificationPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import javax.inject.Inject

/**
 * Delete and fetchTokens wallets
 */
class DeleteWalletInteract @Inject constructor(
    private val walletRepository: com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType,
    private val passwordStore: PasswordStore,
    private val walletVerificationInteractor: com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor,
    private val backupTriggerPreferences: BackupTriggerPreferencesDataSource,
    private val backupSystemNotificationPreferences: BackupSystemNotificationPreferencesDataSource,
    private val fingerprintPreferences: FingerprintPreferencesDataSource,
    private val walletInfoRepository: com.appcoins.wallet.feature.walletInfo.data.WalletInfoRepository
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