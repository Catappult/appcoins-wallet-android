package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.interact.AutoUpdateInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by franciscocalado on 20/07/2018.
 */

class IabPresenter(private val view: IabView,
                   private val autoUpdateInteract: AutoUpdateInteract,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable) {

  fun present() {
    handleAutoUpdate()
  }

  private fun handleAutoUpdate() {
    disposable.add(autoUpdateInteract.getAutoUpdateModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter {
          autoUpdateInteract.isHardUpdateRequired(it.blackList,
              it.updateVersionCode, it.updateMinSdk)
        }
        .doOnSuccess { view.showUpdateRequiredView() }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }
}