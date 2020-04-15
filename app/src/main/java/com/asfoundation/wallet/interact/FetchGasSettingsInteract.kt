package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import io.reactivex.Scheduler
import io.reactivex.Single

class FetchGasSettingsInteract(private val repository: GasSettingsRepositoryType,
                               private val networkScheduler: Scheduler,
                               private val vieScheduler: Scheduler) {

  fun fetch(forTokenTransfer: Boolean): Single<GasSettings> {
    return repository.getGasSettings(forTokenTransfer)
        .subscribeOn(networkScheduler)
        .observeOn(vieScheduler)
  }

}