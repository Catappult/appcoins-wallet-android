package com.asfoundation.wallet.subscriptions.list

import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.SubscriptionModel
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SubscriptionListPresenter(private val view: SubscriptionListView,
                                private val data: SubscriptionListData,
                                private val interactor: UserSubscriptionsInteractor,
                                private val navigator: SubscriptionListNavigator,
                                private val disposables: CompositeDisposable,
                                private val networkScheduler: Scheduler,
                                private val viewScheduler: Scheduler) {

  fun present() {
    loadSubscriptions()
    handleNoNetworkRetryClicks()
    handleGenericRetryClicks()
    handleItemClicks()
  }

  private fun loadSubscriptions() {
    disposables.add(interactor.loadSubscriptions(data.freshReload)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext { onSubscriptions(it) }
        .doOnSubscribe { view.showLoading() }
        .subscribe({}, { onError(it) }))
  }

  private fun handleItemClicks() {
    disposables.add(view.subscriptionClicks()
        .observeOn(viewScheduler)
        .doOnNext { navigator.showSubscriptionDetails(it.first, it.second) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun onSubscriptions(subscriptionModel: SubscriptionModel) {
    val activeSubs = filterActive(subscriptionModel.allSubscriptions)
    val expiredSubs = filterExpired(activeSubs, subscriptionModel.expiredSubscriptions)
    val bothEmpty = activeSubs.isEmpty() && expiredSubs.isEmpty()
    when {
      subscriptionModel.error == SubscriptionModel.Error.NO_NETWORK -> {
        //If we have items from db then we should not show a no network error. If it's empty then we should show to the user
        if (!view.hasItems()) view.showNoNetworkError()
      }
      bothEmpty && !subscriptionModel.fromCache -> view.showNoSubscriptions()
      bothEmpty.not() -> {
        //Both from cache and not from cache
        view.onActiveSubscriptions(activeSubs)
        view.onExpiredSubscriptions(expiredSubs)
        view.showSubscriptions()
      }
      else -> Unit //When both empty and fromCache we should not do anything and wait for the API
    }
  }

  private fun filterExpired(activeSubs: List<SubscriptionItem>,
                            expiredSubscriptions: List<SubscriptionItem>): List<SubscriptionItem> {
    return expiredSubscriptions
        .filter { expiredItem ->
          activeSubs.none { activeItem ->
            activeItem.packageName == expiredItem.packageName && activeItem.sku == expiredItem.sku
          }
        }
  }

  private fun handleNoNetworkRetryClicks() {
    disposables.add(
        view.getRetryNetworkClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showNoNetworkRetryAnimation() }
            .observeOn(networkScheduler)
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { loadSubscriptions() }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleGenericRetryClicks() {
    disposables.add(
        view.getRetryGenericClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.showGenericRetryAnimation() }
            .observeOn(networkScheduler)
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { loadSubscriptions() }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun onError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      view.showGenericError()
    }
  }

  private fun filterActive(userSubscriptionItems: List<SubscriptionItem>): List<SubscriptionItem> {
    return userSubscriptionItems.filter { it.isActiveSubscription() }
        .distinctBy { item -> item.packageName to item.sku }
  }

  fun stop() = disposables.clear()
}
