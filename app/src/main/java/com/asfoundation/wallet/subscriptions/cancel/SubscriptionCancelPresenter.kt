package com.asfoundation.wallet.subscriptions.cancel

import com.asfoundation.wallet.subscriptions.Status
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionCancelPresenter(private val view: SubscriptionCancelView,
                                  private val subscriptionInteractor: UserSubscriptionsInteractor,
                                  private val data: SubscriptionCancelData,
                                  private val navigator: SubscriptionCancelNavigator,
                                  private val disposables: CompositeDisposable,
                                  private val networkScheduler: Scheduler,
                                  private val viewScheduler: Scheduler
) {

  fun present() {
    if (canCancelSubscription(data.subscriptionItem.status)) {
      view.showSubscriptionDetails(data.subscriptionItem)
    } else {
      view.showCancelError()
    }
    handleCancelClicks()
    handleBackClicks()
    handleNoNetworkRetryClicks()
  }

  private fun canCancelSubscription(status: Status): Boolean {
    return status == Status.ACTIVE || status == Status.PAUSED
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
        .flatMapSingle {
          subscriptionInteractor.cancelSubscription(data.subscriptionItem.packageName,
              data.subscriptionItem.uid)
              .observeOn(viewScheduler)
              .doOnSuccess {
                if (it) navigator.showCancelSuccess()
                else view.showCancelError()
              }
        }
        .observeOn(viewScheduler)
        .subscribe({}, { onError(it) }))
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
            .delay(1, TimeUnit.SECONDS)
            .observeOn(networkScheduler)
            .doOnNext { view.showSubscriptionDetails(data.subscriptionItem) }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}