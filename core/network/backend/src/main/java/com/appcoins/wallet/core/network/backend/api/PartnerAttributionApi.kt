package com.appcoins.wallet.core.network.backend.api

import io.reactivex.Single
import retrofit2.http.GET

interface PartnerAttributionApi {
  @GET("appc/cached-apks") fun fetchPackagesForCaching(): Single<List<String>?>
}
