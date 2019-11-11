package com.asfoundation.wallet.wallet_blocked

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletStatusApi {

  @GET("transaction/blocked")
  fun isWalletBlocked(@Query("wallet") wallet: String): Single<WalletStatusResponse>

}