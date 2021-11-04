package com.asfoundation.wallet.verification.credit_card.network

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenTransactionResponse
import com.appcoins.wallet.billing.adyen.VerificationInfoResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BrokerVerificationApi {

  @GET("verification/state")
  fun getVerificationState(@Query("wallet.address") wallet: String,
                           @Query("wallet.signature") walletSignature: String): Single<String>

  @GET("verification/info")
  fun getVerificationInfo(@Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature")
                          walletSignature: String): Single<VerificationInfoResponse>

  @POST("verification/generate")
  fun makePaypalVerificationPayment(@Query("wallet.address") walletAddress: String,
                                    @Query("wallet.signature") walletSignature: String,
                                    @Body
                                    verificationPayment: AdyenPaymentRepository.VerificationPayment): Single<AdyenTransactionResponse>

  @POST("verification/generate")
  fun makeCreditCardVerificationPayment(@Query("wallet.address") walletAddress: String,
                                        @Query("wallet.signature") walletSignature: String,
                                        @Body
                                        verificationPayment: AdyenPaymentRepository.VerificationPayment): Completable

  @POST("verification/validate")
  fun validateCode(@Query("wallet.address") walletAddress: String,
                   @Query("wallet.signature") walletSignature: String,
                   @Body code: String): Completable
}