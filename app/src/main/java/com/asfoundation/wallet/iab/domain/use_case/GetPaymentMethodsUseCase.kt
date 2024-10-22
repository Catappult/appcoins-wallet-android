package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.bdsbilling.BillingRepository
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPaymentMethodsUseCase @Inject constructor(
  private val billingRepository: BillingRepository,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
) {

  suspend operator fun invoke(
    value: String? = null,
    currency: String? = null,
    currencyType: String? = null,
    direct: Boolean? = null,
    transactionType: String? = null,
    packageName: String? = null,
    entityOemId: String? = null,
    address: String? = null,
  ) =
    billingRepository.getPaymentMethods(
      value = value,
      currency = currency,
      currencyType = currencyType,
      direct = direct,
      transactionType = transactionType,
      packageName = packageName,
      entityOemId = entityOemId,
      address = address,
    ).callAsync(networkDispatcher)
}