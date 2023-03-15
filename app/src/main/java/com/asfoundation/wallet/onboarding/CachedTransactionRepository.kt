package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
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

  interface CachedTransactionApi {
    @GET("/transaction/inapp/cached_values")
    fun getCachedTransaction(): Single<CachedTransactionResponse>
  }
}

data class CachedTransactionResponse(
  @SerializedName("url") val referrerUrl: String,
  val product: String,
  val domain: String,
  @SerializedName("callback_url") val callbackUrl: String,
  val currency: String,
  @SerializedName("order_reference") val orderReference: String,
  val value: Double,
  val signature: String
)

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