package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.core.network.backend.api.CachedTransactionApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

class CachedTransactionRepository @Inject constructor(
  val api: CachedTransactionApi,
  val rxSchedulers: RxSchedulers
) {

  fun getCachedTransaction(): Single<CachedTransaction> {
    return api.getCachedTransaction()
      .subscribeOn(rxSchedulers.io)
      .map {
        CachedTransaction(
          it.referrerUrl,
          it.product,
          it.domain,
          it.callbackUrl,
          it.currency,
          it.orderReference,
          it.value,
          it.signature
        )
      }.onErrorReturn {
        CachedTransaction(null, null, null, null, null, null, 0.0, null)
      }
  }
}

data class CachedTransaction(
  val referrerUrl: String?,
  val sku: String?,
  val packageName: String?,
  val callbackUrl: String?,
  val currency: String?,
  val orderReference: String?,
  val value: Double,
  val signature: String?
)