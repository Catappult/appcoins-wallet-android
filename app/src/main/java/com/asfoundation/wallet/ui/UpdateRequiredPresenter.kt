package com.asfoundation.wallet.ui

import com.asfoundation.wallet.interact.AutoUpdateInteract
import io.reactivex.disposables.CompositeDisposable

class UpdateRequiredPresenter(private val activity: UpdateRequiredActivity,
                              private val disposable: CompositeDisposable,
                              private val autoUpdateInteract: AutoUpdateInteract) {

  fun present() {
    handleUpdateClick()
  }

  private fun handleUpdateClick() {
    disposable.add(
        autoUpdateInteract.retrieveRedirectUrl()
            .doOnSuccess { url ->
              if (url != "Error") {
                activity.navigateToStoreAppView(url)
              } else {
                activity.showError()
              }
            }
            .subscribe())
  }

  fun stop() {
    disposable.clear()
  }

}
