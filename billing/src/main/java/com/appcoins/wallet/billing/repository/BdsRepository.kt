package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Observable
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String,
                             skus: List<String>,
                             type: Repository.BillingType,
                             walletAddress: String,
                             walletSignature: String): Single<List<Product>> {
    return Observable.fromIterable(skus)
        .map {
          remoteRepository.getSkuDetails(packageName, it, type.name, walletAddress, walletSignature)
              .toObservable()
        }
        .toList()
        .flatMap { Observable.merge(it).toList() }
  }
}