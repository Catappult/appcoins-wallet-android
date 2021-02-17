package com.asfoundation.wallet.ui.overlay

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class OverlayPresenter(private val view: OverlayView,
                       private val data: OverlayData,
                       private val interactor: OverlayInteractor,
                       private val disposable: CompositeDisposable) {

  fun present() {
    initializeView()
    handleDismissClick()
    handleDiscoverClick()
  }

  private fun initializeView() {
    view.initializeView(data.bottomNavigationItem, data.type)
  }

  private fun handleDiscoverClick() {
    disposable.add(view.discoverClick()
        .doOnNext {
          setHasSeenTooltip()
          view.navigateToPromotions()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDismissClick() {
    disposable.add(Observable.merge(view.dismissClick(), view.overlayClick())
        .doOnNext {
          setHasSeenTooltip()
          view.dismissView()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun setHasSeenTooltip() {
    when (data.type) {
      OverlayType.ALL_PROMOTIONS -> interactor.setHasSeenPromotionTooltip()
      OverlayType.VOUCHERS -> interactor.setHasSeenVoucherTooltip()
    }
  }

  fun stop() = disposable.clear()
}
