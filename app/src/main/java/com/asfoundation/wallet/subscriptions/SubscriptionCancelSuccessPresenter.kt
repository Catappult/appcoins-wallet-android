package com.asfoundation.wallet.subscriptions

import io.reactivex.disposables.CompositeDisposable

class SubscriptionCancelSuccessPresenter(
    private val disposables: CompositeDisposable,
    private val view: SubscriptionCancelSuccessView
) {

  fun present() {
    handleContinueCLicks()
  }

  private fun handleContinueCLicks() {
    disposables.add(
        view.getContinueClicks()
            .doOnNext { view.navigateBack() }
            .subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }

}