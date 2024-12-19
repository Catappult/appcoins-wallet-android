package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.AmazonPayChargePermissionResponse
import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayPaymentRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AmazonPayApi {

  @POST("8.20240911/gateways/amazonpay/transactions")
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Body amazonPayRequest: AmazonPayPaymentRequest
  ): Single<AmazonPayTransaction>

  @PATCH("8.20240911/gateways/amazonpay/transactions/{uid}")
  fun updateCheckoutSessionId(
    @Path("uid") uid: String?,
    @Query("wallet.address") walletAddress: String,
    @Body amazonPayRequest: AmazonPayCheckoutSessionRequest
  ): Completable

  @GET("8.20240911/gateways/amazonpay/charge-permission")
  fun getChargePermission(
    @Query("wallet.address") walletAddress: String,
  ): Single<AmazonPayChargePermissionResponse>

  @POST("8.20240911/gateways/amazonpay/charge-permission/cancel")
  fun deleteChargePermission(
    @Query("wallet.address") walletAddress: String,
  ): Completable

}