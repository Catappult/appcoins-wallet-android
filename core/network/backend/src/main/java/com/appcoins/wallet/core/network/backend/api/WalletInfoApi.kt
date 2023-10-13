package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.WalletInfoResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletInfoApi {
  @GET("/transaction/1.20230807/wallet/{address}/info")
  fun getWalletInfo(@Path("address") address: String, @Query("currency") currency: String?): Single<WalletInfoResponse>
}