package com.appcoins.wallet.core.network.microservices.api;

import com.appcoins.wallet.core.network.microservices.model.TopUpResponse
import com.appcoins.wallet.core.network.microservices.model.TopUpStatus
import com.appcoins.wallet.core.network.microservices.model.TransactionType
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface TopUpApi {
  @GET("8.20200101/transactions")
  fun getTopUpHistory(
    @Query("type") type: TransactionType,
    @Query("status") topUpStatus: TopUpStatus,
    @Query("wallet_from") walletAddress: String
  ): Single<TopUpResponse>
}