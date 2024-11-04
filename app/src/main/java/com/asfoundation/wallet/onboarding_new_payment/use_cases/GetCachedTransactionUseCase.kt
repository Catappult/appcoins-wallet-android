package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.asfoundation.wallet.onboarding.CachedTransaction
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_OSP
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_SDK
import io.reactivex.Single
import javax.inject.Inject

class GetCachedTransactionUseCase @Inject constructor(
  private val cachedTransactionRepository: CachedTransactionRepository
) {
  operator fun invoke(currencyCode: String, amount: Double): Single<CachedTransaction> {
    return cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        val integrationFlow = when {
          (cachedTransaction.type?.uppercase() == PAYMENT_TYPE_SDK) || cachedTransaction.callbackUrl == null ->
            "sdk"

          else -> "osp"
        }
        if (cachedTransaction.value <= 0.0) {
          cachedTransaction.value = amount
        }
        if (cachedTransaction.currency.isNullOrEmpty()) {
          cachedTransaction.currency = currencyCode
        }
        if (integrationFlow == "sdk") {
          cachedTransaction.type = PAYMENT_TYPE_SDK
          cachedTransaction.callbackUrl = null
          cachedTransaction.referrerUrl = null
        } else {
          cachedTransaction.type = PAYMENT_TYPE_OSP
          cachedTransaction.wsPort = null
          cachedTransaction.sdkVersion = null
        }
        Single.just(cachedTransaction)
      }
  }
}
