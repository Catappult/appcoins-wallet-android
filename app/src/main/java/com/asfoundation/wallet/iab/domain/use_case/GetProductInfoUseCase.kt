package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.bdsbilling.BillingRepository
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.TransactionPrice
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetProductInfoUseCase @Inject constructor(
  private val billingRepository: BillingRepository,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
) {

  suspend operator fun invoke(
    packageName: String,
    skuId: String,
    type: BillingSupportedType = BillingSupportedType.INAPP
  ): ProductInfoData? {
    val skuDetails = billingRepository.getSkuDetails(packageName, listOf(skuId), type)
      .callAsync(networkDispatcher)
      .firstOrNull()

    if (skuDetails == null) return null

    return ProductInfoData(
      id = skuDetails.sku,
      packageName = packageName,
      title = skuDetails.title,
      description = skuDetails.description,
      transaction = TransactionPrice(
        base = skuDetails.transactionPrice.base,
        appcoinsAmount = skuDetails.transactionPrice.appcoinsAmount,
        amount = skuDetails.transactionPrice.amount,
        currency = skuDetails.transactionPrice.currency,
        currencySymbol = skuDetails.transactionPrice.currencySymbol,
      )
    )
  }
}