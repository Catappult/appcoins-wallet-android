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
    return cachedBackup.getCachedBackup()
      .flatMap { backupKey ->
        if (backupKey.isNotEmpty()) {
          cachedTransaction.getCachedTransaction()
            .map { transaction ->
              StartMode.PendingPurchaseWithWalletFlow(
                integrationFlow = "sdk",
                sku = transaction.sku,
                packageName = transaction.packageName!!,
                callbackUrl = transaction.callbackUrl,
                currency = transaction.currency,
                orderReference = transaction.orderReference,
                value = transaction.value,
                signature = transaction.signature,
                origin = transaction.origin,
                type = transaction.type,
                oemId = transaction.oemId,
                wsPort = transaction.wsPort,
                backup = backupKey
              )
            }
        } else {
          null
        }
      }.blockingGet()
  }
}

