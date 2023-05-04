package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class UpdateBackupStateFromRecoverUseCase @Inject constructor(
    private val getWalletInfoUseCase: GetWalletInfoUseCase,
    private val backupSuccessLogUseCase: BackupSuccessLogUseCase
) {

  operator fun invoke(): Completable {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
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