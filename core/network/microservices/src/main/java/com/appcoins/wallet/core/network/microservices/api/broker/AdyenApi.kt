package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface AdyenApi {
  @POST("8.20200815/gateways/adyen_v2/transactions")
  @Headers("Content-Type: application/json;format=product_token")
  fun makeAdyenBodyPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body payment: PaymentRequest
  ): Single<AdyenTransactionResponse>

  @POST("8.20200815/gateways/adyen_v2/transactions")
  fun makeAdyenPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body payment: PaymentDetails
  ): Single<AdyenTransactionResponse>

  @GET("8.20200815/gateways/adyen_v2/payment-methods")
  fun loadPaymentInfo(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Query("price.value") value: String,
    @Query("price.currency") currency: String,
    @Query("method") methods: String
  ): Single<PaymentMethodsResponse>


  @GET("8.20200815/gateways/adyen_v2/transactions/{uid}")
  fun getTransaction(
    @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature")
    walletSignature: String
  ): Single<TransactionResponse>


  @Headers("Content-Type: application/json;format=product_token")
  @POST("8.20200815/gateways/adyen_v2/transactions")
  fun makeTokenPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body payment: TokenPayment
  ): Single<AdyenTransactionResponse>

  @PATCH("8.20200815/gateways/adyen_v2/transactions/{uid}")
  fun submitRedirect(
    @Path("uid") uid: String,
    @Query("wallet.address") address: String,
    @Query("wallet.signature") signature: String,
    @Body payment: AdyenPayment
  ): Single<AdyenTransactionResponse>

  @POST("8.20200815/gateways/adyen_v2/disable-recurring")
  fun disablePayments(@Body wallet: DisableWallet): Completable
}