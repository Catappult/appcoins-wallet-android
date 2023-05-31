package com.asfoundation.wallet.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(ActivityComponent::class)
@Module
class TransactionDetailModule {

  @Provides
  fun provideTransactionDetailViewModelFactory(
    findDefaultWalletUseCase: FindDefaultWalletUseCase,
    findNetworkInfoUseCase: FindNetworkInfoUseCase,
    externalBrowserRouter: ExternalBrowserRouter,
    displayChatUseCase: DisplayChatUseCase,
    transactionDetailRouter: TransactionDetailRouter,
    localCurrencyConversionService: LocalCurrencyConversionService
  ): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(
      findDefaultWalletUseCase, findNetworkInfoUseCase,
      externalBrowserRouter, CompositeDisposable(), displayChatUseCase, transactionDetailRouter,
      localCurrencyConversionService
    )
  }
}

class TransactionDetailViewModelFactory(
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
  private val externalBrowserRouter: ExternalBrowserRouter,
  private val compositeDisposable: CompositeDisposable,
  private val displayChatUseCase: DisplayChatUseCase,
  private val transactionDetailRouter: TransactionDetailRouter,
  private val localCurrencyConversionService: LocalCurrencyConversionService
) : ViewModelProvider.Factory {

  @NonNull
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return TransactionDetailViewModel(
      findDefaultWalletUseCase, findNetworkInfoUseCase,
      externalBrowserRouter, compositeDisposable, displayChatUseCase, transactionDetailRouter,
      localCurrencyConversionService
    ) as T
  }
}