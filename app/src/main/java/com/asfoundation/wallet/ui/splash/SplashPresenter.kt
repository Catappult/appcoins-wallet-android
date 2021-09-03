package com.asfoundation.wallet.ui.splash

import android.content.Intent
import android.os.Bundle
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction


class SplashPresenter(private val interactor: SplashInteractor,
                      private val navigator: SplashNavigator,
                      private val viewScheduler: Scheduler,
                      private val ioScheduler: Scheduler,
                      private val disposables: CompositeDisposable) {

  private var hasStartedAuth = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      hasStartedAuth = it.getBoolean(HAS_STARTED_AUTH)
    }
    if (!hasStartedAuth) handleNavigation()

  }

  private fun handleNavigation() {
    disposables.add(
        interactor.getAutoUpdateModel()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
              if (interactor.isHardUpdateRequired(blackList, updateVersionCode, updateMinSdk)) {
                navigator.navigateToAutoUpdate()
              } else {
                if (interactor.hasAuthenticationPermission()) {
                  navigator.showAuthenticationActivity()
                  hasStartedAuth = true
                } else {
                  navigator.firstScreenNavigation(interactor.shouldShowOnboarding())
                }
              }
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(HAS_STARTED_AUTH, hasStartedAuth)
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == SplashNavigator.AUTHENTICATION_REQUEST_CODE) {
      if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
        navigator.firstScreenNavigation(interactor.shouldShowOnboarding())
      } else {
        navigator.finish()
      }
    }
  }

  private companion object {
    private const val HAS_STARTED_AUTH = "started_auth"
  }
}