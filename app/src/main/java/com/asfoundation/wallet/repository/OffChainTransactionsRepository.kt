package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletHistory
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.DateFormat
import java.util.*

class OffChainTransactionsRepository(private val api: TransactionsApi,
                                     private val dateFormatter: DateFormat) {


  fun getTransactionsSync(wallet: String, versionCode: String, startingDate: Long? = null,
                          endingDate: Long? = null, offset: Int, sort: String?,
                          limit: Int): Call<WalletHistory> {

    return api.transactionHistorySync(wallet, versionCode,
        startingDate = startingDate?.let { dateFormatter.format(it) },
        endingDate = endingDate?.let { dateFormatter.format(it) }, offset = offset, sort = sort,
        limit = limit, languageCode = Locale.getDefault().language)
  }

  fun getTransactionsById(wallet: String,
                          txList: List<String>): Single<List<WalletHistory.Transaction>> {
    return api.getTransactionsById(wallet, txList.toTypedArray())
  }

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
        @Query("lang_code") languageCode: String): Call<WalletHistory>

    fun getTransactionsById(
        @Query("wallet") wallet: String,
        @Query("transaction_list")
        transactions: Array<String>): Single<List<WalletHistory.Transaction>>
  }
}
