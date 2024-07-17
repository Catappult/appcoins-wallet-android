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
  operator fun invoke(currencyCode: String, amount: Double):  Single<CachedTransaction> {
    return cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        if (cachedTransaction.value <= 0.0) {
           cachedTransaction.value = amount
        }
        if (cachedTransaction.currency.isNullOrEmpty()) {
          cachedTransaction.currency = currencyCode
        }
        if (cachedTransaction.type?.uppercase() == PAYMENT_TYPE_SDK) {
          cachedTransaction.callbackUrl = null
          cachedTransaction.referrerUrl = null
        } else if (cachedTransaction.type?.uppercase() == PAYMENT_TYPE_OSP) {
            cachedTransaction.wsPort = null
        }
        Single.just(cachedTransaction)
      }
  }
}
