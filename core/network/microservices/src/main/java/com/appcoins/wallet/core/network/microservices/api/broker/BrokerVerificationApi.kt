package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.AdyenTransactionResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.core.network.microservices.model.VerificationPayment
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface BrokerVerificationApi {

  @GET("8.20200815/gateways/adyen_v2/verification/state")
  fun getVerificationState(
    @Query("wallet.address") wallet: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
  ): Single<String>

  @GET("8.20200815/gateways/adyen_v2/verification/info")
  fun getVerificationInfo(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
  ): Single<VerificationInfoResponse>

  @POST("8.20200815/gateways/adyen_v2/verification/generate")
  fun makePaypalVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
    @Body
    verificationPayment: VerificationPayment
  ): Single<AdyenTransactionResponse>

  @POST("8.20200815/gateways/adyen_v2/verification/generate")
  fun makeCreditCardVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
    @Body
    verificationPayment: VerificationPayment
  ): Completable

  @POST("8.20200815/gateways/adyen_v2/verification/validate")
  fun validateCode(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
    @Body code: String
  ): Completable
}