package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedBackupRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_OSP
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_SDK
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface PendingPurchaseFlowUseCase {
  operator fun invoke(): StartMode.PendingPurchaseFlow?
}

@BoundTo(supertype = PendingPurchaseFlowUseCase::class)
class PendingPurchaseFlowUseCaseImpl @Inject constructor(
  private val cachedTransaction: CachedTransactionRepository,
  private val cachedBackup: CachedBackupRepository
) : PendingPurchaseFlowUseCase {
  override operator fun invoke(): StartMode.PendingPurchaseFlow? {
    var startModeResult: StartMode.PendingPurchaseFlow? = null
    val cachedTransaction = cachedTransaction.getCachedTransaction().blockingGet()
    val cachedBackupKey = cachedBackup.getCachedBackup().blockingGet()
    if (cachedBackupKey != null) {
      if (cachedTransaction?.type?.uppercase() == PAYMENT_TYPE_SDK) {
        startModeResult = StartMode.PendingPurchaseFlow(
          integrationFlow = "sdk",
          sku = cachedTransaction.sku,
          packageName = cachedTransaction.packageName ?: "",
          callbackUrl = null,
          currency = cachedTransaction.currency,
          orderReference = cachedTransaction.orderReference,
          value = cachedTransaction.value,
          signature = null,
          origin = cachedTransaction.origin,
          type = cachedTransaction.type,
          oemId = cachedTransaction.oemId,
          wsPort = cachedTransaction.wsPort,
          backup = cachedBackupKey
        )
      }
    } else if (cachedTransaction?.type?.uppercase() == PAYMENT_TYPE_OSP) {
      startModeResult = StartMode.PendingPurchaseFlow(
        integrationFlow = "osp",
        sku = cachedTransaction.sku,
        packageName = cachedTransaction.packageName ?: "",
        callbackUrl = cachedTransaction.callbackUrl,
        currency = cachedTransaction.currency,
        orderReference = cachedTransaction.orderReference,
        value = cachedTransaction.value,
        signature = cachedTransaction.signature,
        origin = cachedTransaction.origin,
        type = cachedTransaction.type,
        oemId = null,
        wsPort = null,
        backup = null
      )
    }
    return startModeResult
  }
}
