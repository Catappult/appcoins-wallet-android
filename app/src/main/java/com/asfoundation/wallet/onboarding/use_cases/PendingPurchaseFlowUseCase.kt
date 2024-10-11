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
    val integrationFlow = when {
      (cachedBackupKey != null && cachedTransaction?.type?.uppercase() == PAYMENT_TYPE_SDK) || cachedTransaction?.callbackUrl == null ->
        "sdk"
      else -> "osp"
    }
    if (!cachedBackupKey.isNullOrEmpty() || cachedTransaction?.callbackUrl != null) {
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
        sdkVersion = cachedTransaction.sdkVersion,
        backup = cachedBackupKey
      )
    }
    return startModeResult
  }
}
