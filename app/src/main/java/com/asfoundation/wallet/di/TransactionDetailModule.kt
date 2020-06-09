package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.subscriptions.SubscriptionRepository
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


@Module
class TransactionDetailModule {

  @Provides
  fun provideTransactionDetailViewModelFactory(
      findDefaultNetworkInteract: FindDefaultNetworkInteract,
      findDefaultWalletInteract: FindDefaultWalletInteract,
      externalBrowserRouter: ExternalBrowserRouter,
      subscriptionRepository: SubscriptionRepository): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(findDefaultNetworkInteract,
        findDefaultWalletInteract, externalBrowserRouter, CompositeDisposable(),
        subscriptionRepository, Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun externalBrowserRouter() = ExternalBrowserRouter()
}