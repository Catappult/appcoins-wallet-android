package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import io.reactivex.Scheduler
import io.reactivex.Single

class FetchGasSettingsInteract(private val repository: GasSettingsRepositoryType,
                               private val networkScheduler: Scheduler,
                               private val vieScheduler: Scheduler) {

  fun fetch(forTokenTransfer: Boolean): Single<GasSettings> {
    //corrects for gas price too low on legacy type 0 transactions (after the EIP-1559).
    val gasMultiplier = 1.15
    return repository.getGasSettings(forTokenTransfer, gasMultiplier)
        .subscribeOn(networkScheduler)
        .observeOn(vieScheduler)
  }

}