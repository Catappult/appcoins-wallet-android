package com.asfoundation.wallet.backup.use_cases

import preferences.BackupTriggerPreferences
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShouldShowBackupTriggerUseCase @Inject constructor(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val backupTriggerPreferences: BackupTriggerPreferences
) {

  companion object {
    private const val DISMISS_PERIOD = 30L
  }

  operator fun invoke(walletAddress: String): Single<Boolean> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
      .flatMap {
        if (it.hasBackup) {
          Single.just(false)
        } else {
          meetsLastDismissCondition(walletAddress)
        }
      }
  }

  private fun meetsLastDismissCondition(walletAddress: String): Single<Boolean> {
    return Single.create {
      val savedTime =
        backupTriggerPreferences.getBackupTriggerSeenTime(walletAddress)
      val currentTime = System.currentTimeMillis()
      val result = currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
      it.onSuccess(result)
    }
  }
}