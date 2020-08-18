package com.asfoundation.wallet.ui

import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


class SplashPresenter(
    private val view: SplashView,
    private val preferencesRepositoryType: PreferencesRepositoryType,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val autoUpdateInteract: AutoUpdateInteract) {

  fun present() {
    handleAutoUpdate()
  }

  private fun handleAutoUpdate() {
    disposables.add(autoUpdateInteract.getAutoUpdateModel(true)
        .subscribeOn(ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
          if (autoUpdateInteract.isHardUpdateRequired(blackList,
                  updateVersionCode, updateMinSdk)) {
            view.navigateToAutoUpdate()
          } else {
            if (preferencesRepositoryType.hasAuthenticationPermission()) {
              view.showAuthenticationActivity()
            } else {
              view.firstScreenNavigation()
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()

}