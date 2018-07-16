package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Sku
import com.appcoins.wallet.billing.repository.entity.SkuDetails
import io.reactivex.Single

internal class BdsBilling(private val repository: Repository,
                          private val errorMapper: BillingThrowableCodeMapper) : Billing {

  override fun isInAppSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.INAPP).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.SUBS).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getInappSkuDetails(packageName: String, skuIds: List<String>): Single<SkuDetails> {
    return repository.getSkuDetails(packageName, skuIds).map { map(it) }
  }

  private fun map(skus: List<Sku>): SkuDetails {
    return SkuDetails(skus)
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND
}