package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.FetchPublicKeyResponse
import com.appcoins.wallet.core.network.backend.model.FetchUserKeyResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface LoginApi {

  @GET("/appc/user_wallet/key")
  fun fetchUserKey(
    @Header("Authorization") jwt: String
  ): Single<FetchUserKeyResponse>

  @GET("/appc/public-key/wallet_crypto")
  fun fetchPublicKey(): Single<FetchPublicKeyResponse>

}
