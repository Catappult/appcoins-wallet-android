package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class EarnAppcoinsPresenter(private val view: EarnAppcoinsView,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler) {
  fun present() {
    handleBackClick()
    handleDiscoverClick()
  }

  private fun handleDiscoverClick() {
    disposables.add(view.discoverButtonClick()
        .observeOn(viewScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackClick() {
    disposables.add(Observable.merge(view.backButtonClick(), view.backPressed())
        .observeOn(viewScheduler)
        .doOnNext { view.navigateBack() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun destroy() {
    disposables.clear()
  }
}
