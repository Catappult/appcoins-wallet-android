package com.appcoins.wallet.core.network.bds.api

import com.appcoins.wallet.core.network.bds.model.GetWalletResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface BdsApiSecondary {
  @GET("7/bds/apks/package/getOwnerWallet")
  fun getWallet(@Query("package_name") packageName: String): Single<GetWalletResponse>
}

