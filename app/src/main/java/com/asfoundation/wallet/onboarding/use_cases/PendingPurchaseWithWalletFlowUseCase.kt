package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedBackupRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface PendingPurchaseWithWalletFlowUseCase {
  operator fun invoke(): StartMode.PendingPurchaseWithWalletFlow?
}

@BoundTo(supertype = PendingPurchaseWithWalletFlowUseCase::class)
class PendingPurchaseWithWalletFlowUseCaseImpl @Inject constructor(
  private val cachedTransaction: CachedTransactionRepository,
  private val cachedBackup: CachedBackupRepository
) : PendingPurchaseWithWalletFlowUseCase {

  override operator fun invoke(): StartMode.PendingPurchaseWithWalletFlow? {
    val cachedBackupKey = cachedBackup.getCachedBackup().blockingGet()
    var startModeResult: StartMode.PendingPurchaseWithWalletFlow? = null
    if (cachedBackupKey != null) {
      val cachedTransaction = cachedTransaction.getCachedTransaction().blockingGet()
      if (cachedTransaction != null) {
        startModeResult = StartMode.PendingPurchaseWithWalletFlow(
          integrationFlow = "sdk",
          sku = cachedTransaction.sku,
          packageName = cachedTransaction.packageName!!,
          callbackUrl = cachedTransaction.callbackUrl,
          currency = cachedTransaction.currency,
          orderReference = cachedTransaction.orderReference,
          value = cachedTransaction.value,
          signature = cachedTransaction.signature,
          origin = cachedTransaction.origin,
          type = cachedTransaction.type,
          oemId = cachedTransaction.oemId,
          wsPort = cachedTransaction.wsPort,
          backup = cachedBackupKey
        )
      }
    }
    return startModeResult
  }
}

