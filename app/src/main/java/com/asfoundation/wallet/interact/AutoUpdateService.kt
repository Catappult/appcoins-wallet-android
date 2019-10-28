package com.asfoundation.wallet.interact

import io.reactivex.Single
import retrofit2.http.GET

class AutoUpdateService(private val api: AutoUpdateApi) {

  fun loadAutoUpdateModel(): Single<AutoUpdateModel> {
    return api.getAutoUpdateInfo()
        .map {
          AutoUpdateModel(it.softUpdate.versionCode, it.softUpdate.minSdk,
              it.blackList)
        }
        .onErrorReturn { AutoUpdateModel() }
  }

  interface AutoUpdateApi {

    @GET("tobe/implemented")
    fun getAutoUpdateInfo(): Single<AutoUpdateResponse>

  }

}
