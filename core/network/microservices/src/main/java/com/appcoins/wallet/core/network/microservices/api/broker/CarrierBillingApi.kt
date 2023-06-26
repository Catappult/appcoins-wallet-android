package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface CarrierBillingApi {
  @POST("8.20210329/gateways/dimoco/transactions")
  fun makePayment(
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body carrierTransactionBody: CarrierTransactionBody
  )
      : Single<CarrierCreateTransactionResponse>

  @GET("8.20210329/gateways/dimoco/transactions/{uid}")
  fun getPayment(
    @Path("uid") uid: String,
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
  ): Observable<TransactionResponse>

  @GET("8.20210329/dimoco/countries")
  fun getAvailableCountryList(): Single<CountryListResponse>
}