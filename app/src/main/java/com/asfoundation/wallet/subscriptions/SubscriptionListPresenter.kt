package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionListPresenter(
    private val subscriptionInteract: SubscriptionInteract,
    private val disposables: CompositeDisposable,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler,
    private val view: SubscriptionListView
) {

  fun present() {
    loadSubscriptions()
    handleRetryClick()
  }

  private fun loadSubscriptions() {
    disposables.add(
        subscriptionInteract.loadSubscriptions()
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSubscribe { onSubscribe() }
            .doOnSuccess(this::onSubscriptions)
            .doOnError(this::onError)
            .subscribe()
    )
  }

  private fun onSubscriptions(subscriptionModel: SubscriptionModel) {
    if (subscriptionModel.isEmpty) {
      view.showNoSubscriptions()
    } else {
      view.showSubscriptions()
      view.onActiveSubscriptions(subscriptionModel.activeSubscriptions)
      view.onExpiredSubscriptions(subscriptionModel.expiredSubscriptions)
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

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { loadSubscriptions() }
        .doOnError(Throwable::printStackTrace)
        .subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }

}
