package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.AdyenTransactionResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationPayment
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BrokerVerificationApi {

  @GET("8.20200815/gateways/adyen_v2/verification/state")
  fun getVerificationState(
    @Query("wallet.address") wallet: String,
  ): Single<String>

  @GET("8.20240314/gateways/adyen_v2/verification/info")
  fun getVerificationInfo(
    @Query("wallet.address") walletAddress: String,
  ): Single<VerificationInfoResponse>

  @POST("8.20240314/gateways/adyen_v2/verification/generate")
  fun makePaypalVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Body verificationPayment: VerificationPayment
  ): Single<AdyenTransactionResponse>

  @POST("8.20240314/gateways/adyen_v2/verification/generate")
  fun makeCreditCardVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Body verificationPayment: VerificationPayment
  ): Completable

  @POST("8.20240314/gateways/adyen_v2/verification/validate")
  fun validateCode(
    @Query("wallet.address") walletAddress: String,
    @Body code: String
  ): Completable
}
