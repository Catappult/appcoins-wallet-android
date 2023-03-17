package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface IpApi {
  @GET("appc/countrycode")
  fun myIp(): Single<IpResponse?>?
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class IpResponse {
  @JsonProperty("countryCode")
  var countryCode: String? = null
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
    @Query("lang_code") languageCode: String
  ): Call<WalletHistory>

  fun getTransactionsById(
    @Query("wallet") wallet: String,
    @Query("transaction_list")
    transactions: Array<String>
  ): Single<List<WalletHistory.Transaction>>
}

interface PromoCodeBackendApi {
  @GET("gamification/perks/promo_code/{promoCodeString}/")
  fun getPromoCodeBonus(
    @Path("promoCodeString") promoCodeString: String
  ): Single<PromoCodeBonusResponse>
}

interface RedeemGiftBackendApi {
  @POST("gamification/giftcard/{giftcard_key}/redeem")
  fun redeemGiftCode(
    @Path("giftcard_key") giftCode: String,
    @Header("authorization") authorization: String
  ): Completable
}

interface WalletInfoApi {
  @GET("/transaction/wallet/{address}/info")
  fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
}

interface CachedTransactionApi {
  @GET("/transaction/inapp/cached_values")
  fun getCachedTransaction(): Single<CachedTransactionResponse>
}

interface BackupLogApi {
  @POST("/transaction/wallet/backup/")
  fun logBackupSuccess(
    @Header("authorization") authorization: String
  ): Completable
}


interface TokenToFiatApi {
  @GET("appc/value")
  fun getAppcToFiatRate(
    @Query("currency") currency: String?
  ): Observable<AppcToFiatResponseBody?>?
}

interface AutoUpdateApi {
  @GET("appc/wallet_version")
  fun getAutoUpdateInfo(): Single<AutoUpdateResponse>
}

interface GasService {
  @GET("transaction/gas_price")
  fun getGasPrice(): Single<GasPrice>
}