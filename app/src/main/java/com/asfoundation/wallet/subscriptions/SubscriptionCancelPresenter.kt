package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionCancelPresenter(
    private val view: SubscriptionCancelView,
    private val subscriptionInteract: SubscriptionInteract,
    private val disposables: CompositeDisposable,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler
) {

  fun present(packageName: String) {
    loadSubscriptionDetails(packageName)
    handleCancelClicks(packageName)
    handleBackClicks()
    handleNoNetworkRetryClicks(packageName)
  }

  private fun loadSubscriptionDetails(packageName: String) {
    disposables.add(
        subscriptionInteract.loadSubscriptionDetails(packageName)
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSubscribe { view.showLoading() }
            .doOnSuccess { if (it is ActiveSubscriptionDetails) view.showSubscriptionDetails(it) }
            .subscribe({}, { onError(it) }))
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      view.showCancelError()
    }
  }

  private fun handleCancelClicks(packageName: String) {
    disposables.add(
        view.getCancelClicks()
            .doOnNext { view.showLoading() }
            .subscribeOn(viewScheduler)
            .observeOn(networkScheduler)
            .flatMapCompletable {
              subscriptionInteract.cancelSubscription(packageName)
                  .observeOn(viewScheduler)
                  .doOnComplete {
                    view.showCancelSuccess()
                  }
            }
            .observeOn(viewScheduler)
            .subscribe({}, { onError(it) }))
  }

  private fun handleBackClicks() {
    disposables.add(
        view.getBackClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.navigateBack() }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleNoNetworkRetryClicks(packageName: String) {
    disposables.add(
        view.getRetryNetworkClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showNoNetworkRetryAnimation() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(networkScheduler)
            .doOnNext { loadSubscriptionDetails(packageName) }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

}