package com.asfoundation.wallet.subscriptions.details

import com.asfoundation.wallet.subscriptions.Status
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import com.appcoins.wallet.core.utils.common.extensions.isNoNetworkException
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SubscriptionDetailsPresenter(private val view: SubscriptionDetailsView,
                                   private val navigator: SubscriptionDetailsNavigator,
                                   private val data: SubscriptionDetailsData,
                                   private val userSubscriptionsInteractor: UserSubscriptionsInteractor,
                                   private val disposables: CompositeDisposable,
                                   private val viewScheduler: Scheduler) {

  fun present() {
    view.setupTransitionName(data.transitionName)
    setupUi()
    handleCancelClicks()
    handleRenewSubscriptionClicks()
    handleRetryClicks()
  }

  private fun handleRetryClicks() {
    disposables.add(
        view.getRetryClicks()
            .observeOn(viewScheduler)
            .doOnEach {
              view.showDetails()
              setupUi()
            }
            .subscribe({}, { it.printStackTrace() })
    )
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

  private fun handleRenewSubscriptionClicks() {
    disposables.add(
        view.getRenewSubscriptionClicks()
            .flatMap {
              view.showLoading()
              userSubscriptionsInteractor.activateSubscription(data.subscriptionItem.packageName,
                  data.subscriptionItem.uid)
                  .subscribeOn(Schedulers.io())
                  .observeOn(viewScheduler)
                  .doOnComplete {
                    navigator.showRenewSuccess()
                  }
                  .doOnError { onError(it) }
                  .onErrorComplete()
                  .andThen(Observable.just(Unit))
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      view.showRenewError()
    }
  }

  fun stop() = disposables.clear()
}
