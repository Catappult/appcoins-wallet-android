package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionDetailsPresenter(
    private val subscriptionInteract: SubscriptionInteract,
    private val disposables: CompositeDisposable,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler,
    private val view: SubscriptionDetailsView
) {

  fun present(packageName: String) {
    loadSubscriptionDetails(packageName)
    handleCancelClicks()
    handleNoNetworkRetryClicks(packageName)
    handleGenericRetryClicks(packageName)
  }

  private fun loadSubscriptionDetails(packageName: String) {
    disposables.add(
        Single.fromCallable { view.showLoading() }
            .subscribeOn(viewScheduler)
            .observeOn(networkScheduler)
            .flatMap { subscriptionInteract.loadSubscriptionDetails(packageName) }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnSuccess(this::onSubscriptionDetails)
            .subscribe({}, { onError(it) })
    )
  }

  private fun onSubscriptionDetails(subscriptionDetails: SubscriptionDetails) {
    if (subscriptionDetails is ActiveSubscriptionDetails) {
      view.setActiveDetails(subscriptionDetails)
    } else if (subscriptionDetails is ExpiredSubscriptionDetails) {
      view.setExpiredDetails(subscriptionDetails)
    }
    view.showDetails()
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      view.showGenericError()
    }
  }

  private fun handleCancelClicks() {
    disposables.add(
        view.getCancelClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.cancelSubscription() }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleNoNetworkRetryClicks(packageName: String) {
    disposables.add(
        view.getRetryNetworkClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showNoNetworkRetryAnimation() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(networkScheduler)
            .doOnNext {
              loadSubscriptionDetails(packageName)
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleGenericRetryClicks(packageName: String) {
    disposables.add(
        view.getRetryGenericClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showGenericRetryAnimation() }
            .delay(1, TimeUnit.SECONDS)
            .doOnNext {
              loadSubscriptionDetails(packageName)
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

}
