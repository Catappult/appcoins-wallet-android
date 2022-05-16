package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.backup.repository.preferences.BackupSystemNotificationPreferences
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShouldShowSystemNotificationUseCase @Inject constructor(
  private val sharedPreferencesRepository: SharedPreferencesRepository,
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferences
) {

  companion object {
    private const val PURCHASE_NOTIFICATION_THRESHOLD = 2
    private const val DISMISS_PERIOD = 30L
  }

  operator fun invoke(walletInfo: WalletInfo): Single<Boolean> {
    return if (walletInfo.hasBackup.not()) {
      Single.just(meetsLastDismissCondition(walletInfo.wallet) && meetsCountConditions(walletInfo.wallet))
    } else {
      Single.just(false)
    }
  }

  private fun meetsLastDismissCondition(walletAddress: String): Boolean {
    val savedTime =
      backupSystemNotificationPreferences.getDismissedBackupSystemNotificationSeenTime(walletAddress)
    val currentTime = System.currentTimeMillis()
    return currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
  }

  private fun meetsCountConditions(walletAddress: String): Boolean {
    val count = sharedPreferencesRepository.getWalletPurchasesCount(walletAddress)
    return count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0
  }
}