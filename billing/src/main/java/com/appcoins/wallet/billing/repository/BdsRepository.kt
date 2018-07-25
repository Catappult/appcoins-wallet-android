package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.BillingThrowableCodeMapper
import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository,
                             private val errorMapper: BillingThrowableCodeMapper) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String, skus: List<String>,
                             type: Repository.BillingType): Single<List<Product>> {
    return remoteRepository.getSkuDetails(packageName, skus, type.name)
        .onErrorResumeNext {
          it.printStackTrace()
          Single.error(errorMapper.mapException(it))
        }
  }
}