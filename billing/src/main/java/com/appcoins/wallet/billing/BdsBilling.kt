package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single

internal class BdsBilling(private val merchantName: String,
                          private val repository: Repository,
                          private val errorMapper: BillingThrowableCodeMapper) : Billing {

  override fun isInAppSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.INAPP).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.SUBS).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(skus: List<String>, type: String): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus, Repository.BillingType.valueOf(type))
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND
}