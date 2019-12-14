package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.subscriptions.SubscriptionRepository

class TransactionDetailViewModelFactory(
    private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val externalBrowserRouter: ExternalBrowserRouter,
    private val subscriptionRepository: SubscriptionRepository) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, subscriptionRepository) as T
  }

}