package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.AutoUpdateResponse
import io.reactivex.Single
import retrofit2.http.GET

interface AutoUpdateApi {
  @GET("appc/wallet_version")
  fun getAutoUpdateInfo(): Single<AutoUpdateResponse>
}