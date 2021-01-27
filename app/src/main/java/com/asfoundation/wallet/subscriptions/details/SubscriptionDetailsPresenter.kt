package com.asfoundation.wallet.subscriptions.details

import com.asfoundation.wallet.subscriptions.Status
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SubscriptionDetailsPresenter(private val view: SubscriptionDetailsView,
                                   private val navigator: SubscriptionDetailsNavigator,
                                   private val data: SubscriptionDetailsData,
                                   private val disposables: CompositeDisposable,
                                   private val viewScheduler: Scheduler) {

  fun present() {
    setupUi()
    handleCancelClicks()
  }

  private fun setupUi() {
    val item = data.subscriptionItem
    if (item.isActiveSubscription()) {
      view.setActiveDetails(item)
    } else if (item.status == Status.EXPIRED) {
      view.setExpiredDetails(item)
    }
  }

  private fun handleCancelClicks() {
    disposables.add(
        view.getCancelClicks()
            .observeOn(viewScheduler)
            .doOnNext {
              navigator.showCancelSubscription(data.subscriptionItem, view.retrieveSharedElement())
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}
