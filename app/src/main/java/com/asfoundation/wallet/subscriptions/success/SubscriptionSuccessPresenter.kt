package com.asfoundation.wallet.subscriptions.success

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SubscriptionSuccessPresenter(private val view: SubscriptionSuccessView,
                                   private val data: SubscriptionSuccessData,
                                   private val navigator: SubscriptionSuccessNavigator,
                                   private val disposables: CompositeDisposable,
                                   private val viewScheduler: Scheduler
) {

  fun present() {
    view.setupUi(data.successType)
    handleContinueCLicks()
  }

  private fun handleContinueCLicks() {
    disposables.add(view.getContinueClicks()
        .observeOn(viewScheduler)
        .doOnNext { navigator.navigateToSubscriptionList() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun navigateToListSubscriptions() = navigator.navigateToSubscriptionList()

  fun stop() = disposables.clear()
}