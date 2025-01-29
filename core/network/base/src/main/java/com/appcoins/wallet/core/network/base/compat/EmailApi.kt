package com.appcoins.wallet.core.network.base.compat

import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailApi {
  @POST("appc/email/")
  fun postUserEmail(@Body body: WalletEmailRequest): Completable
}