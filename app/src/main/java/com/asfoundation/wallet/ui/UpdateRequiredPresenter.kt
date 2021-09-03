package com.asfoundation.wallet.ui

import com.asfoundation.wallet.interact.AutoUpdateInteract
import io.reactivex.disposables.CompositeDisposable

class UpdateRequiredPresenter(private val activity: UpdateRequiredView,
                              private val disposable: CompositeDisposable,
                              private val autoUpdateInteract: AutoUpdateInteract) {

  fun present() {
    handleUpdateClick()
  }

  private fun handleUpdateClick() {
    disposable.add(activity.updateClick()
        .doOnNext { activity.navigateToIntent(autoUpdateInteract.buildUpdateIntent()) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    activity.showError()
  }

  fun stop() = disposable.clear()
}
