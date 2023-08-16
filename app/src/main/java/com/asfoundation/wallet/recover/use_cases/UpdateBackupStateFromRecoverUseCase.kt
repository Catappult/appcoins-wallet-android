package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Completable
import javax.inject.Inject

class UpdateBackupStateFromRecoverUseCase @Inject constructor(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val backupSuccessLogUseCase: BackupSuccessLogUseCase
) {

  operator fun invoke(): Completable {
    return getWalletInfoUseCase(null, cached = false)
      .flatMapCompletable {
        if (!it.hasBackup) {
          return@flatMapCompletable backupSuccessLogUseCase(it.wallet).andThen(Completable.complete())
        }
        Completable.complete()
      }
  }
}