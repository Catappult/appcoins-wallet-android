package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedBackupRepository
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface RestoreGuestWalletFlowUseCase {
  operator fun invoke(): StartMode.RestoreGuestWalletFlow?
}

@BoundTo(supertype = RestoreGuestWalletFlowUseCase::class)
class RestoreGuestWalletFlowUseCaseImpl @Inject constructor(
  private val cachedBackup: CachedBackupRepository
) : RestoreGuestWalletFlowUseCase {
  override operator fun invoke(): StartMode.RestoreGuestWalletFlow? {
    val cachedBackupKey = cachedBackup.getCachedBackup().blockingGet()
    return if (cachedBackupKey != null) {
      StartMode.RestoreGuestWalletFlow(
        backup = cachedBackupKey,
      )
    } else {
      null
    }
  }
}
