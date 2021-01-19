package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


@Module
class TransactionDetailModule {

  @Provides
  fun provideTransactionDetailViewModelFactory(
      findDefaultNetworkInteract: FindDefaultNetworkInteract,
      findDefaultWalletInteract: FindDefaultWalletInteract,
      externalBrowserRouter: ExternalBrowserRouter, supportInteractor: SupportInteractor,
      transactionDetailRouter: TransactionDetailRouter): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, CompositeDisposable(), supportInteractor, transactionDetailRouter,
        Schedulers.io())
  }

  @Provides
  fun provideTransactionDetailRouter() = TransactionDetailRouter()

  @Provides
  fun externalBrowserRouter() = ExternalBrowserRouter()
}