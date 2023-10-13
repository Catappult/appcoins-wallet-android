package com.asfoundation.wallet.recover.use_cases

import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class UpdateBackupStateFromRecoverUseCase @Inject constructor(
        private val getWalletInfoUseCase: GetWalletInfoUseCase,
        private val backupSuccessLogUseCase: BackupSuccessLogUseCase
) {

  operator fun invoke(): Completable {
    return getWalletInfoUseCase(null, cached = false)
        .flatMapCompletable {
          if (!it.hasBackup) {

            return@flatMapCompletable rxCompletable(Dispatchers.IO) {
              backupSuccessLogUseCase(it.wallet)
            }
          }
          Completable.complete()
        }
  }
}