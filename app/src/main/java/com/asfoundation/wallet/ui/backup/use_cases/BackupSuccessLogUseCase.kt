package com.asfoundation.wallet.ui.backup.use_cases

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.ui.backup.repository.BackupRepository
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BackupSuccessLogUseCase @Inject constructor(private val ewtObtainer: EwtAuthenticatorService,
                                                  private val backupRepository: BackupRepository) {

  operator fun invoke(address: String): Completable {
    return ewtObtainer.getEwtAuthenticationWithAddress(address)
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { backupRepository.logBackupSuccess(it) }
  }
}