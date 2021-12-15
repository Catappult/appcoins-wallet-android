package com.asfoundation.wallet.ui.backup.success

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class BackupSuccessLogUseCase(private val ewtObtainer: EwtAuthenticatorService,
                              private val backupSuccessLogRepository: BackupSuccessLogRepository) {
  operator fun invoke(address: String): Completable {
    return ewtObtainer.getEwtAuthenticationWithAddress(address)
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { backupSuccessLogRepository.logBackupSuccess(it) }
  }
}