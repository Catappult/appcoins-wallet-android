package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.*
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
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

  @GET(value = "/appc/wallet/{wallet}/history")
  suspend fun getTransactionHistory(
    @Path("wallet") wallet: String,
    @Query("limit") limit: Int = 10,
    @Query("offset") offset: Int = 0,
    @Query("from") startingDate: String? = null,
    @Query("to") endingDate: String? = null,
    @Query("default_currency") defaultCurrency: String,
  ): Response<List<TransactionResponse>>

  fun getTransactionsById(
    @Query("wallet") wallet: String,
    @Query("transaction_list") transactions: Array<String>
  ): Single<List<WalletHistory.Transaction>>

  @GET(value = "/transaction/wallet/invoices/{invoice_id}/pdf/")
  suspend fun getInvoiceById(
    @Path("invoice_id") invoiceId: String,
    @Header("authorization") authorization: String
  ): Response<InvoiceResponse>

  @GET(value = "/transaction/wallet/invoices/countries/")
  suspend fun getCountriesByLanguage(
    @Query("lang_code") languageCode: String,
  ): Response<List<CountriesResponse>>

  @POST(value = "/transaction/wallet/invoices/user_information/")
  suspend fun savePersonalInformation(
    @Header("authorization") authorization: String,
    @Body personalInformation: PersonalInformationRequest,
  ): Response<PersonalInformationResponse>

  @GET(value = "/transaction/wallet/invoices/user_information/")
  suspend fun getPersonalInformation(
    @Header("authorization") authorization: String,
  ): Response<PersonalInformationRequest>
}
