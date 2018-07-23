package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return remoteRepository.getPurchases(packageName, walletAddress, walletSignature, type)
  }
}