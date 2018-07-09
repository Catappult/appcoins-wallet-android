package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.Repository
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository) : Repository {
  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }
}