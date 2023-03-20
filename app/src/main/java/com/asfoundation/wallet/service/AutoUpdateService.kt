package com.asfoundation.wallet.service

import com.appcoins.wallet.core.network.backend.api.AutoUpdateApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
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
}
