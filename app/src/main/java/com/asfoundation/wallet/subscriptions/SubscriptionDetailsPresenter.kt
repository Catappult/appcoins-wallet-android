package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
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
    handleBackClicks()
  }

  private fun loadSubscriptionDetails(packageName: String) {
    disposables.add(
        subscriptionInteract.loadSubscriptionDetails(packageName)
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSubscribe { onSubscribe() }
            .doOnSuccess(this::onSubscriptionDetails)
            .doOnError(this::onError)
            .subscribe()
    )
  }

  private fun onSubscriptionDetails(subscriptionDetails: SubscriptionDetails) {
    view.showDetails()
    if (subscriptionDetails.status == SubscriptionStatus.ACTIVE) {
      view.showActiveDetails(subscriptionDetails)
    } else {
      view.showExpiredDetails(subscriptionDetails)
    }
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    }
  }

  private fun onSubscribe() {
    view.showLoading()
  }

  private fun handleCancelClicks() {
    disposables.add(
        view.getCancelClicks()
            .doOnNext { view.cancelSubscription() }
            .subscribeOn(viewScheduler)
            .subscribe()
    )
  }

  private fun handleBackClicks() {
    disposables.add(
        view.getBackClicks()
            .doOnNext { view.navigateBack() }
            .doOnError(Throwable::printStackTrace)
            .subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }

}
