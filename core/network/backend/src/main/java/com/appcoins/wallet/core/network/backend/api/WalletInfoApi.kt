package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.WalletInfoResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface WalletInfoApi {
  @GET("/transaction/wallet/{address}/info")
  fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
}