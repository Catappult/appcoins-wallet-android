package com.asfoundation.wallet.fingerprint


import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.disposables.CompositeDisposable


class FingerprintPresenter(private val view: FingerprintView,
                           private val disposables: CompositeDisposable,
                           private val preferencesRepositoryType: PreferencesRepositoryType) {
  fun present() {
    view.setSwitchState(preferencesRepositoryType.hasAuthenticationPermission())
    handleSwitchClick()
  }

  private fun handleSwitchClick() {
    disposables.add(view.getSwitchClick()
        .doOnNext { preferencesRepositoryType.setAuthenticationPermission(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()


}
