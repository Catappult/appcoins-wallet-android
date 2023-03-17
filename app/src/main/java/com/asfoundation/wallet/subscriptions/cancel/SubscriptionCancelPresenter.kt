package com.asfoundation.wallet.subscriptions.cancel

import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import com.appcoins.wallet.core.utils.common.extensions.isNoNetworkException
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionCancelPresenter(private val view: SubscriptionCancelView,
                                  private val subscriptionInteractor: UserSubscriptionsInteractor,
                                  private val data: SubscriptionCancelData,
                                  private val navigator: SubscriptionCancelNavigator,
                                  private val disposables: CompositeDisposable,
                                  private val networkScheduler: Scheduler,
                                  private val viewScheduler: Scheduler) {

  fun present() {
    view.setTransitionName(data.transitionName)
    view.showSubscriptionDetails(data.subscriptionItem)
    handleCancelClicks()
    handleBackClicks()
    handleNoNetworkRetryClicks()
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      view.showCancelError()
    }
  }

  private fun handleCancelClicks() {
    disposables.add(view.getCancelClicks()
        .doOnNext { view.showLoading() }
        .subscribeOn(viewScheduler)
        .observeOn(networkScheduler)
        .flatMap {
          subscriptionInteractor.cancelSubscription(data.subscriptionItem.packageName,
              data.subscriptionItem.uid)
              .observeOn(viewScheduler)
              .doOnComplete {
                navigator.showCancelSuccess()
              }
              .doOnError { onError(it) }
              .onErrorComplete()
              .andThen(Observable.just(Unit))
        }
        .observeOn(viewScheduler)
        .subscribe {})
  }

  private fun handleBackClicks() {
    disposables.add(view.getBackClicks()
        .observeOn(viewScheduler)
        .doOnNext { navigator.navigateBack() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleNoNetworkRetryClicks() {
    disposables.add(
        view.getRetryNetworkClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showNoNetworkRetryAnimation() }
            .observeOn(networkScheduler)
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { view.showSubscriptionDetails(data.subscriptionItem) }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}