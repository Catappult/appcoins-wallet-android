package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import javax.inject.Inject

class SetResponseCodeWebSocketUseCase
@Inject
constructor(private val cachedTransactionRepository: CachedTransactionRepository) {
  operator fun invoke(responseCode: Int) =
    cachedTransactionRepository.setResponseCodeToPaymentWebSocket(responseCode)
}
