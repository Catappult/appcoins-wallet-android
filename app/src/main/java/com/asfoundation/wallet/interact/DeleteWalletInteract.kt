package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Delete and fetchTokens wallets
 */
class DeleteWalletInteract @Inject constructor(private val walletRepository: WalletRepositoryType,
                                               private val passwordStore: PasswordStore,
                                               private val walletVerificationInteractor: WalletVerificationInteractor,
                                               private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                                               private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  fun delete(address: String): Completable {
    return passwordStore.getPassword(address)
        .flatMapCompletable { walletRepository.deleteWallet(address, it) }
        .andThen(walletVerificationInteractor.removeWalletVerificationStatus(address))
        .andThen(backupRestorePreferencesRepository.removeWalletRestoreBackup(address))
        .andThen(backupRestorePreferencesRepository.removeBackupNotificationSeenTime(address))
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