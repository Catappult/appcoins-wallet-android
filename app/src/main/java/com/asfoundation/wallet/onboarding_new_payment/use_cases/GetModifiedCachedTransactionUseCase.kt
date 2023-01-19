package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.asfoundation.wallet.onboarding.CachedTransaction
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import io.reactivex.Single
import javax.inject.Inject

class GetModifiedCachedTransactionUseCase @Inject constructor(
  private val bdsRepository: BdsRepository,
  private val cachedTransactionRepository: CachedTransactionRepository
) {

  operator fun invoke(): Single<CachedTransaction> {
    return cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        bdsRepository.getSkuDetails(
          cachedTransaction.packageName!!,
          mutableListOf(cachedTransaction.sku!!),
          BillingSupportedType.INAPP
        )
          .map {
            cachedTransaction.copy(
              value = it[0].transactionPrice.amount,
              currency = it[0].transactionPrice.currency
            )
          }
      }
  }
}

