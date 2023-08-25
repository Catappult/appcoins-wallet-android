package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.TransactionOverviewResponse
import com.appcoins.wallet.core.network.backend.model.BackendTransactionType
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionOverviewApi {
  @GET("transaction/")
  @JvmSuppressWildcards
  fun getTransactionOverviewList(
    @Query("wallet") walletAddress: String?,
    @Query("transaction_types") transactionTypes: List<BackendTransactionType>?,
    @Query("limit") limit: Int?,
    @Query("offset") offset: Int?,
    ): Single<List<TransactionOverviewResponse>>
}
