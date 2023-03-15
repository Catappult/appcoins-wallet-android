package com.asfoundation.wallet.service

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.entity.AutoUpdateResponse
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
import retrofit2.http.GET
import javax.inject.Inject

class AutoUpdateService @Inject constructor(
  private val api: AutoUpdateApi,
  private val rxSchedulers: RxSchedulers
) {

  fun loadAutoUpdateModel(): Single<AutoUpdateModel> {
    return api.getAutoUpdateInfo()
      .subscribeOn(rxSchedulers.io)
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
