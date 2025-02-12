package com.appcoins.wallet.core.network.flagr.api

import com.appcoins.wallet.core.network.flagr.model.FlagrRequest
import com.appcoins.wallet.core.network.flagr.model.FlagrResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface FlagrNetworkApi {

  @POST("evaluation")
  fun getFeatureFlag(
    @Body request: FlagrRequest,
  ): Single<FlagrResponse>

}
