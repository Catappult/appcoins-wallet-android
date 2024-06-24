package com.appcoins.wallet.core.network.backend.api


import com.appcoins.wallet.core.network.backend.model.WalletEmailRequest
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface EmailApi {
  @POST("appc/email/")
  fun postUserEmail(
    @Header("authorization") authorization: String,
    @Body body: WalletEmailRequest
  ): Completable
}