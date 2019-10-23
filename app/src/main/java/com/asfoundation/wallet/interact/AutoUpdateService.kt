package com.asfoundation.wallet.interact

import io.reactivex.Single
import retrofit2.http.GET

class AutoUpdateService(private val api: AutoUpdateApi) {

  fun loadAutoUpdateModel(): Single<AutoUpdateModel> {
    return api.getAutoUpdateInfo()
        .map {
          AutoUpdateModel(it.versionCode, it.redirectUrl, it.minSdk,
              it.updateStores.contains("Aptoide"), it.updateStores.contains("Google Play"))
        }
        .onErrorReturn { AutoUpdateModel() }
  }

  interface AutoUpdateApi {

    @GET("tobe/implemented")
    fun getAutoUpdateInfo(): Single<AutoUpdateResponse>
  }

}
