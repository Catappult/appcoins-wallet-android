package com.asfoundation.wallet.di

import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(ActivityComponent::class)
@Module
class TransactionDetailModule {

  @Provides
  fun provideTransactionDetailViewModelFactory(findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                               findNetworkInfoUseCase: FindNetworkInfoUseCase,
                                               externalBrowserRouter: ExternalBrowserRouter,
                                               displayChatUseCase: DisplayChatUseCase,
                                               transactionDetailRouter: TransactionDetailRouter,
                                               localCurrencyConversionService: LocalCurrencyConversionService): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(findDefaultWalletUseCase, findNetworkInfoUseCase,
        externalBrowserRouter, CompositeDisposable(), displayChatUseCase, transactionDetailRouter,
        localCurrencyConversionService)
  }

  @Provides
  fun provideTransactionDetailRouter() = TransactionDetailRouter()

  @Provides
  fun externalBrowserRouter() = ExternalBrowserRouter()
}