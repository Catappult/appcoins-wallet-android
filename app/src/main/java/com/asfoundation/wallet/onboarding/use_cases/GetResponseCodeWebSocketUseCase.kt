package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import javax.inject.Inject

class GetResponseCodeWebSocketUseCase
@Inject
constructor(private val cachedTransactionRepository: CachedTransactionRepository) {
  operator fun invoke() =
    cachedTransactionRepository.getResponseCodeToPaymentWebSocket()
}
