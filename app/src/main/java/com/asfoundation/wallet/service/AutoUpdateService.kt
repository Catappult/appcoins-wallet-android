package com.asfoundation.wallet.service

import com.asfoundation.wallet.entity.AutoUpdateResponse
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
import retrofit2.http.GET

class AutoUpdateService(private val api: AutoUpdateApi) {

  fun loadAutoUpdateModel(): Single<AutoUpdateModel> {
    return api.getAutoUpdateInfo()
        .map {
          AutoUpdateModel(it.latestVersion.versionCode, it.latestVersion.minSdk, it.blackList)
        }
        .onErrorReturn { AutoUpdateModel() }
  }

  interface AutoUpdateApi {

    @GET("appc/wallet_version")
    fun getAutoUpdateInfo(): Single<AutoUpdateResponse>

  }

}
