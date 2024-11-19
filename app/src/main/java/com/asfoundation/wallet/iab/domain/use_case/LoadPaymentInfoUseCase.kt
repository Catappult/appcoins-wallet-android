package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository.Methods
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class LoadPaymentInfoUseCase @Inject constructor(
  private val adyenPaymentRepository: AdyenPaymentRepository,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
) {
  suspend operator fun invoke(
    methods: Methods,
    value: String,
    currency: String,
    walletAddress: String,
    ewt: String
  ) =
    adyenPaymentRepository.loadPaymentInfo(
      methods = methods,
      value = value,
      currency = currency,
      walletAddress = walletAddress,
      ewt = ewt,
    ).callAsync(networkDispatcher)
}