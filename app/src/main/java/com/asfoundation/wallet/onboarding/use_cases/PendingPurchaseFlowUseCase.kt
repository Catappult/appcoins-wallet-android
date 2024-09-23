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
    val integrationFlow = if (cachedBackupKey != null) {
      if (cachedTransaction?.type?.uppercase() == PAYMENT_TYPE_SDK) {
        "sdk"
      } else {
        "osp"
      }
    } else {
      if (cachedTransaction?.callbackUrl != null) {
        "osp"
      } else {
        "sdk"
      }
    }
    if (cachedBackupKey != null || cachedTransaction?.value != 0.0) {
      startModeResult = StartMode.PendingPurchaseFlow(
        integrationFlow = integrationFlow,
        sku = cachedTransaction.sku,
        packageName = cachedTransaction.packageName ?: "",
        callbackUrl = cachedTransaction.callbackUrl,
        currency = cachedTransaction.currency,
        orderReference = cachedTransaction.orderReference,
        value = cachedTransaction.value,
        signature = cachedTransaction.signature,
        origin = cachedTransaction.origin,
        type = if (integrationFlow == "sdk") PAYMENT_TYPE_SDK else PAYMENT_TYPE_OSP,
        oemId = cachedTransaction.oemId,
        wsPort = cachedTransaction.wsPort,
        backup = cachedBackupKey
      )
    }
    return startModeResult
  }
}
