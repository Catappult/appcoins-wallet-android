package com.asfoundation.wallet.interact

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.appcoins.wallet.sharedpreferences.BackupSystemNotificationPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Delete and fetchTokens wallets
 */
class DeleteWalletInteract @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val walletVerificationInteractor: com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor,
  private val backupTriggerPreferences: BackupTriggerPreferencesDataSource,
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferencesDataSource,
  private val fingerprintPreferences: FingerprintPreferencesDataSource,
  private val walletInfoRepository: WalletInfoRepository,
  private val registerFirebaseTokenUseCase: RegisterFirebaseTokenUseCase,
  private val setActiveWalletUseCase: SetActiveWalletUseCase,
  private val schedulers: RxSchedulers,
) {

  fun delete(address: String): Completable = passwordStore.getPassword(address)
    .flatMapCompletable { walletRepository.deleteWallet(address, it) }
    .andThen(registerFirebaseTokenUseCase.unregisterFirebaseToken(Wallet(address)))
    .andThen(walletVerificationInteractor.removeAllWalletVerificationStatus(address))
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
    .subscribeOn(schedulers.io)
    .observeOn(schedulers.main)

  private fun setNewWallet(): Completable = walletRepository.fetchWallets()
    .filter { it.isNotEmpty() }
    .map { it.first() }
    .flatMapCompletable { setActiveWalletUseCase(it) }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}