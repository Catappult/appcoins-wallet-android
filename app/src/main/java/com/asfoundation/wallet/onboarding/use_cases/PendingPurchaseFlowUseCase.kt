package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface PendingPurchaseFlowUseCase {
  operator fun invoke(): StartMode.PendingPurchaseFlow?
}

@BoundTo(supertype = PendingPurchaseFlowUseCase::class)
class PendingPurchaseFlowUseCaseImpl @Inject constructor(
  private val cachedTransaction: CachedTransactionRepository
) : PendingPurchaseFlowUseCase {
  override operator fun invoke(): StartMode.PendingPurchaseFlow? {
    val cachedTransaction = cachedTransaction.getCachedTransaction().blockingGet()
    return if (cachedTransaction.packageName != null) {
      StartMode.PendingPurchaseFlow(
        integrationFlow = "osp",
        sku = cachedTransaction.sku!!,
        packageName = cachedTransaction.packageName,
        callbackUrl = cachedTransaction.callbackUrl,
        currency = cachedTransaction.currency,
        orderReference = cachedTransaction.orderReference,
        value = cachedTransaction.value,
        signature = cachedTransaction.signature
      )
    } else {
      null
    }
  }
}
