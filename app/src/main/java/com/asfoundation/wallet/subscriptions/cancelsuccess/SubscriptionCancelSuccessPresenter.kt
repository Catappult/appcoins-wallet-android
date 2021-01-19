package com.asfoundation.wallet.subscriptions.cancelsuccess

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SubscriptionCancelSuccessPresenter(private val view: SubscriptionCancelSuccessView,
                                         private val navigator: SubscriptionCancelSuccessNavigator,
                                         private val disposables: CompositeDisposable,
                                         private val viewScheduler: Scheduler
) {

  fun present() {
    handleContinueCLicks()
  }

  private fun handleContinueCLicks() {
    disposables.add(view.getContinueClicks()
        .observeOn(viewScheduler)
        .doOnNext { navigator.navigateBack() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}