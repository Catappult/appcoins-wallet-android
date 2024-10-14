package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import io.reactivex.Completable
import javax.inject.Inject

class SendBackupToEmailUseCase
@Inject
constructor(
  private val createBackupUseCase: CreateBackupUseCase,
  private val backupRepository: BackupRepository,
  private val schedulers: RxSchedulers
) {
  operator fun invoke(walletAddress: String, password: String, email: String): Completable {
    return createBackupUseCase(walletAddress, password)
      .observeOn(schedulers.io)
      .flatMapCompletable { backupData ->
        backupRepository.sendBackupEmail(walletAddress, backupData, email)
      }
  }
}
