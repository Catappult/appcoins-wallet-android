package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.backend.model.TransactionType
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApi {
  @GET("transaction/")
  fun getTransactionList(
    @Query("wallet") walletAddress: String? = null,
    @Query("transaction_types") transactionTypes: List<TransactionType>? = null,
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    ): Single<List<TransactionResponse>>
}
