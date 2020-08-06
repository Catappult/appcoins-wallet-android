package com.asfoundation.wallet.ui

import android.util.Log
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


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
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
          if (autoUpdateInteract.isHardUpdateRequired(blackList,
                  updateVersionCode, updateMinSdk)) {
            view.navigateToAutoUpdate()
          } else {
            if (preferencesRepositoryType.hasAuthenticationPermission()) {
              view.showAuthenticationActivity()
            } else {
              Log.d("TAG123", "FIRST RUN")
              view.firstScreenNavigation()
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }


  fun stop() {
    disposables.clear()
  }


}