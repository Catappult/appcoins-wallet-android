package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.subscriptions.SubscriptionRepository
import io.reactivex.Scheduler

class TransactionDetailViewModelFactory(
    private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val externalBrowserRouter: ExternalBrowserRouter,
    private val subscriptionRepository: SubscriptionRepository,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, subscriptionRepository, networkScheduler, viewScheduler) as T
  }

}