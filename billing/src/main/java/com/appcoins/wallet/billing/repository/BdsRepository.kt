package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String,
                             skuIds: List<String>,
                             type: Repository.BillingType): Single<List<Product>> {
    return remoteRepository.getSkuDetails(packageName, skuIds, type.name)
  }
}