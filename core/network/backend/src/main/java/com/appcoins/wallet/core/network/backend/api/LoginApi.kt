package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.WalletInfoResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface LoginApi {

  @GET("/appc/user_wallet/key")
  fun fetchUserKey(
    @Header("Authorization") jwt: String
  ): Single<String> //TODO:  model

}
