package com.appcoins.wallet.core.network.eskills.api

import com.appcoins.wallet.core.network.eskills.model.AppInfo
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface AppDataApi {
  @GET("app/getMeta")
  fun getMeta( @Path("package_name") packageName: String): Single<AppInfo>

}