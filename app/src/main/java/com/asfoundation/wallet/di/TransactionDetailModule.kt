package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class TransactionDetailModule {
  @Provides
  fun provideTransactionDetailViewModelFactory(
      findDefaultNetworkInteract: FindDefaultNetworkInteract,
      findDefaultWalletInteract: FindDefaultWalletInteract,
      externalBrowserRouter: ExternalBrowserRouter, supportRepository: SupportRepository,
      transactionDetailRouter: TransactionDetailRouter): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, CompositeDisposable(), supportRepository, transactionDetailRouter)
  }

  @Provides
  fun provideTransactionDetailRouter() = TransactionDetailRouter()

  @Provides
  fun externalBrowserRouter() = ExternalBrowserRouter()
}