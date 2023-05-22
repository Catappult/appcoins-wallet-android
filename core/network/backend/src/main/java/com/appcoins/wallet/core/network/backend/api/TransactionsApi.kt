package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.*
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface TransactionsApi {

  @GET("appc/wallethistory")
  fun transactionHistorySync(
    @Query("wallet") wallet: String,
    @Query("version_code") versionCode: String,
    @Query("type") transactionType: String = "all",
    @Query("offset") offset: Int = 0,
    @Query("from") startingDate: String? = null,
    @Query("to") endingDate: String? = null,
    @Query("sort") sort: String? = "desc",
    @Query("limit") limit: Int,
    @Query("lang_code") languageCode: String
  ): Call<WalletHistory>

  fun getTransactionsById(
    @Query("wallet") wallet: String,
    @Query("transaction_list")
    transactions: Array<String>
  ): Single<List<WalletHistory.Transaction>>
}
