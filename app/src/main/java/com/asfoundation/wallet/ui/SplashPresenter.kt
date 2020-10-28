package com.asfoundation.wallet.ui

import android.os.Bundle
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

  private var hasStartedAuth = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let { hasStartedAuth = it.getBoolean(HAS_STARTED_AUTH) }
    if (!hasStartedAuth) handleNavigation()
  }

  private fun handleNavigation() {
    disposables.add(autoUpdateInteract.getAutoUpdateModel(true)
        .subscribeOn(ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
          if (autoUpdateInteract.isHardUpdateRequired(blackList, updateVersionCode, updateMinSdk)) {
            view.navigateToAutoUpdate()
          } else {
            if (preferencesRepositoryType.hasAuthenticationPermission()) {
              view.showAuthenticationActivity()
              hasStartedAuth = true
            } else {
              view.firstScreenNavigation()
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(HAS_STARTED_AUTH, hasStartedAuth)
  }

  private companion object {
    private const val HAS_STARTED_AUTH = "started_auth"
  }
}