package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.backup.repository.preferences.BackupSystemNotificationPreferences
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import javax.inject.Inject

class ShouldShowSystemNotificationUseCase @Inject constructor(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val sharedPreferencesRepository: SharedPreferencesRepository,
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferences
) {

  companion object {
    private const val PURCHASE_NOTIFICATION_THRESHOLD = 2
  }

  operator fun invoke(walletAddress: String): Boolean {
    val count = sharedPreferencesRepository.getWalletPurchasesCount(walletAddress)
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
      .map { walletInfo ->
        if (walletInfo.hasBackup.not() && count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0) {
          backupSystemNotificationPreferences.hasDismissedBackupSystemNotification(walletAddress)
            .not()
        } else {
          false
        }
      }.blockingGet()
  }
}