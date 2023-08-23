package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShouldShowBackupTriggerUseCase @Inject constructor(
    private val getWalletInfoUseCase: GetWalletInfoUseCase,
    private val backupTriggerPreferences: BackupTriggerPreferencesDataSource,
    private val dispatchers: Dispatchers
) {

  companion object {
    private const val DISMISS_PERIOD = 30L
  }

  suspend operator fun invoke(walletAddress: String): Boolean {
    return withContext(dispatchers.io) {
      val walletHaveBackup =
          getWalletInfoUseCase(null, cached = false, updateFiat = false).await()
      return@withContext if (walletHaveBackup.hasBackup) {
        false
      } else {
        meetsLastDismissCondition(walletAddress)
      }
    }
  }

  private fun meetsLastDismissCondition(walletAddress: String): Boolean {
    val savedTime = backupTriggerPreferences.getBackupTriggerSeenTime(walletAddress)
    val currentTime = System.currentTimeMillis()
    return currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
  }
}