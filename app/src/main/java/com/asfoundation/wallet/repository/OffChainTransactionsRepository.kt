package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletHistory
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class OffChainTransactionsRepository(private val api: TransactionsApi) {

  fun getTransactions(wallet: String, versionCode: String,
                      offChainOnly: Boolean): Single<WalletHistory> {
    return if (offChainOnly) {
      api.transactionHistory(wallet, versionCode, "offchain")
    } else {
      api.transactionHistory(wallet, versionCode)
    }
  }

  fun getTransactionsSync(wallet: String, versionCode: String, startingDate: String? = null,
                          endingDate: String? = null, offset: Int,
                          sort: String?, limit: Int): Call<WalletHistory> {
    return api.transactionHistorySync(wallet, versionCode, startingDate = startingDate,
        endingDate = endingDate, offset = offset, sort = sort, limit = limit)
  }

  interface TransactionsApi {
    @GET("appc/wallethistory")
    fun transactionHistory(
        @Query("wallet") wallet: String,
        @Query("version_code") versionCode: String,
        @Query("type") transactionType: String = "all",
        @Query("offset") offset: Int = 0,
        @Query("from") startingDate: String? = null,
        @Query("to") endingDate: String? = null,
        @Query("sort") sort: String? = "desc"): Single<WalletHistory>

    @GET("appc/wallethistory")
    fun transactionHistorySync(
        @Query("wallet") wallet: String,
        @Query("version_code") versionCode: String,
        @Query("type") transactionType: String = "all",
        @Query("offset") offset: Int = 0,
        @Query("from") startingDate: String? = null,
        @Query("to") endingDate: String? = null,
        @Query("sort") sort: String? = "desc",
        @Query("limit") limit: Int): Call<WalletHistory>
  }
}
