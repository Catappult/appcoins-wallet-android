package com.asfoundation.wallet.subscriptions

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SubscriptionCancelSuccessPresenter(
    private val view: SubscriptionCancelSuccessView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler
) {

  fun present() {
    handleContinueCLicks()
  }

  private fun handleContinueCLicks() {
    disposables.add(
        view.getContinueClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.navigateBack() }
            .subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }

}