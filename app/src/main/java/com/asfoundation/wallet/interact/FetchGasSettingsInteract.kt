package com.asfoundation.wallet.interact

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class FetchGasSettingsInteract @Inject constructor(private val repository: GasSettingsRepositoryType,
                               private val rxSchedulers: RxSchedulers
) {

  fun fetch(forTokenTransfer: Boolean): Single<GasSettings> {
    return repository.getGasSettings(forTokenTransfer)
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.main)
  }
}