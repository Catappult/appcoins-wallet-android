package com.asfoundation.wallet.di

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.C
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.support.SupportInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class TransactionDetailModule {
  @Provides
  fun provideTransactionDetailViewModelFactory(
      findDefaultNetworkInteract: FindDefaultNetworkInteract,
      externalBrowserRouter: ExternalBrowserRouter, supportInteractor: SupportInteractor,
      transactionDetailRouter: TransactionDetailRouter): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(data, interactor, findDefaultNetworkInteract,
        walletService, externalBrowserRouter, CompositeDisposable(), supportInteractor,
        transactionDetailRouter)
  }

  @Provides
  fun provideTransactionDetailRouter() = TransactionDetailRouter()

  @Provides
  fun externalBrowserRouter() = ExternalBrowserRouter()
}