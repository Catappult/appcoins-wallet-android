package com.asfoundation.wallet.fingerprint


import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


class FingerprintPresenter(private val view: FingerprintView,
                           private val viewScheduler: Scheduler,
                           private val ioScheduler: Scheduler,
                           private val disposables: CompositeDisposable,
                           private val preferencesRepositoryType: PreferencesRepositoryType) {
  fun present() {
    view.setSwitchState(preferencesRepositoryType.hasAuthenticationPermission())
    handleImageClick()
    handleSwitchClick()
  }

  private fun handleImageClick() {
    disposables.add(view.getImageClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showClickText() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleSwitchClick() {
    disposables.add(view.getSwitchClick()
        .doOnNext {
          preferencesRepositoryType.setAuthenticationPermission(it)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

}
