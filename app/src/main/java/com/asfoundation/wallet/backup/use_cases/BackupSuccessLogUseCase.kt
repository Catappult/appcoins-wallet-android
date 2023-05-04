package com.asfoundation.wallet.backup.use_cases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.backup.repository.BackupRepository
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupSuccessLogUseCase @Inject constructor(
    private val ewtObtainer: EwtAuthenticatorService,
    private val backupRepository: BackupRepository,
    private val dispatchers: Dispatchers
) {

  suspend operator fun invoke(address: String): Unit {

    withContext(dispatchers.io) {
      val ewt = ewtObtainer.getEwtAuthenticationWithAddress(address).await()
      backupRepository.logBackupSuccess(ewt)
    }
  }
}