package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.AutoUpdateService
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single

class AutoUpdateRepository(private val autoUpdateService: AutoUpdateService) {

  private var autoUpdateModel = AutoUpdateModel()

  fun loadAutoUpdateModel(invalidateCache: Boolean): Single<AutoUpdateModel> {
    if (autoUpdateModel.isValid() && !invalidateCache) {
      return Single.just(autoUpdateModel)
    }
    return autoUpdateService.loadAutoUpdateModel()
        .doOnSuccess { if (it.isValid()) autoUpdateModel = it }
  }
}