package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedBackupRepository
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface RestoreGuestWalletUseCase {
  operator fun invoke(): StartMode.RestoreGuestWalletFlow?
}

@BoundTo(supertype = RestoreGuestWalletUseCase::class)
class RestoreGuestWalletUseCaseImpl
@Inject
constructor(private val cachedBackup: CachedBackupRepository) : RestoreGuestWalletUseCase {
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
