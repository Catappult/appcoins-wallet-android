package com.asfoundation.wallet.ui.overlay

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class OverlayPresenter(private val view: OverlayView,
                       private val interactor: OverlayInteractor,
                       private val disposable: CompositeDisposable) {

  fun present() {
    handleDismissClick()
    handleDiscoverClick()
  }

  private fun handleDiscoverClick() {
    disposable.add(view.discoverClick()
        .doOnNext {
          interactor.setHasSeenPromotionTooltip()
          view.navigateToPromotions()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDismissClick() {
    disposable.add(Observable.merge(view.dismissClick(), view.overlayClick())
        .doOnNext {
          interactor.setHasSeenPromotionTooltip()
          view.dismissView()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()
}
