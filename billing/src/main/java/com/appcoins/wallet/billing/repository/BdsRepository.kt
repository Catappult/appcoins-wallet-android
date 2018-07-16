package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Sku
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String, skuIds: List<String>): Single<List<Sku>> {
    return remoteRepository.getSkuDetails(packageName, skuIds)
  }
}