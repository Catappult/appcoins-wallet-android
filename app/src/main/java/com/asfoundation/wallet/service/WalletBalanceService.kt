package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigInteger

interface WalletBalanceService {

  @GET("transaction/balance")
  fun getWalletBalance(@Query("wallet") walletAddress: String): Single<WalletBalance>


  companion object {
    const val API_BASE_URL = BuildConfig.BACKEND_HOST
  }

}

data class WalletBalance(val appc: BigInteger, val eth: BigInteger)