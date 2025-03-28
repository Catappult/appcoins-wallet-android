package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.core.network.base.JwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import io.reactivex.Completable
import javax.inject.Inject

class BackupSuccessLogUseCase @Inject constructor(
  private val jwtAuthenticatorService: JwtAuthenticatorService,
  private val backupRepository: BackupRepository,
  private val schedulers: RxSchedulers
) {
  operator fun invoke(address: String): Completable {
    return Completable.defer {
      jwtAuthenticatorService.getJwtAuthenticationWithAddress(address)
        .observeOn(schedulers.io)
        .flatMapCompletable { jwt -> backupRepository.logBackupSuccess(jwt) }
    }
  }
}