package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.InvoiceResponse
import com.appcoins.wallet.core.network.backend.model.TransactionPagingResponse
import com.appcoins.wallet.core.network.backend.model.WalletHistory
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

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

  @GET("/appc/1.20240925/wallet/{wallet}/history")
  suspend fun getTransactionHistory(
    @Path("wallet") wallet: String,
    @Query("limit") limit: Int = 10,
    @Query("cursor") cursor: String? = null,
    @Query("from") startingDate: String? = null,
    @Query("to") endingDate: String? = null,
    @Query("default_currency") defaultCurrency: String,
    @Query("lang_code") languageCode: String
  ): Response<TransactionPagingResponse>

  fun getTransactionsById(
    @Query("wallet") wallet: String,
    @Query("transaction_list") transactions: Array<String>
  ): Single<List<WalletHistory.Transaction>>

  @GET(value = "/transaction/wallet/invoices/{invoice_id}/pdf/")
  suspend fun getInvoiceById(
    @Path("invoice_id") invoiceId: String,
    @Header("authorization") authorization: String
  ): Response<InvoiceResponse>
}
