package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.interact.AutoUpdateInteract
import io.reactivex.disposables.CompositeDisposable

class IabUpdateRequiredPresenter(private val view: IabUpdateRequiredView,
                                 private val disposables: CompositeDisposable,
                                 private val autoUpdateInteract: AutoUpdateInteract) {

  fun present() {
    handleUpdateClick()
    handleCancelClick()
  }

  private fun handleCancelClick() {
    disposables.add(view.cancelClick()
        .doOnNext { view.close() }
        .subscribe())
  }

  private fun handleUpdateClick() {
    disposables.add(view.updateClick()
        .flatMapSingle { autoUpdateInteract.retrieveRedirectUrl() }
        .doOnNext { url -> view.navigateToStoreAppView(url) }
        .subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
