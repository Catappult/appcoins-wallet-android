package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.repository.BackupRepository
import com.appcoins.wallet.feature.walletInfo.data.authentication.EwtAuthenticatorService
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