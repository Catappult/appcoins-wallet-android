package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface VkPayApi {

  @POST("8.20230921/gateways/vkpay/transactions")
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body vkPayPaymentRequest: VkPayPaymentRequest
  ): Single<VkTransactionResponse>


  @POST("8.20231001/mock/vkpay")
  fun changeVkTransactionStatusDev(
    @Query("transaction_uid") transactionUid: String,
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body status: String?
  ): Single<Boolean>
}