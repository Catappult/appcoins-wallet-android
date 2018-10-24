package com.appcoins.wallet.bdsbilling.repository

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface BdsApiSecondary {
  @GET("7/bds/apks/package/getOwnerWallet")
  fun getWallet(@Query("package_name") packageName: String): Single<GetWalletResponse>
}

data class GetWalletResponse(val data: Data)

data class Data(val address: String)
