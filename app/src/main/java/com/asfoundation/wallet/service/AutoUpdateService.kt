package com.asfoundation.wallet.service

import com.asfoundation.wallet.entity.AutoUpdateResponse
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
import retrofit2.http.GET

class AutoUpdateService(private val api: AutoUpdateApi) {

  fun loadAutoUpdateModel(): Single<AutoUpdateModel> {
    return api.getAutoUpdateInfo()
        .map {
          AutoUpdateModel(it.softUpdate.versionCode,
              it.softUpdate.minSdk,
              it.blackList)
        }
        .onErrorReturn { AutoUpdateModel() }
  }

  interface AutoUpdateApi {

    @GET("tobe/implemented")
    fun getAutoUpdateInfo(): Single<AutoUpdateResponse>

  }

}
