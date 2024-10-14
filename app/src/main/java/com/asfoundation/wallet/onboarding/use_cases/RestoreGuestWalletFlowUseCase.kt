package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedBackupRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_SDK
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface RestoreGuestWalletUseCase {
  operator fun invoke(): StartMode.RestoreGuestWalletFlow?
}

@BoundTo(supertype = RestoreGuestWalletUseCase::class)
class RestoreGuestWalletUseCaseImpl @Inject constructor(
  private val cachedTransaction: CachedTransactionRepository,
  private val cachedBackup: CachedBackupRepository
) : RestoreGuestWalletUseCase {
  override operator fun invoke(): StartMode.RestoreGuestWalletFlow? {
    val cachedTransaction = cachedTransaction.getCachedTransaction().blockingGet()
    val cachedBackupKey = cachedBackup.getCachedBackup().blockingGet()
    return if (cachedBackupKey != null && cachedBackupKey != "") {
      if (cachedTransaction?.sku != null) {
        StartMode.RestoreGuestWalletFlow(
          backup = cachedBackupKey,
          integrationFlow = when {
            (cachedTransaction.type?.uppercase() == PAYMENT_TYPE_SDK) ||
                cachedTransaction.callbackUrl == null -> "sdk"

            else -> "osp"
          },
          sku = cachedTransaction.sku,
          packageName = cachedTransaction.packageName ?: "",
        )
      } else {
        StartMode.RestoreGuestWalletFlow(
          backup = cachedBackupKey
        )
      }
    } else {
      null
    }
  }
}
