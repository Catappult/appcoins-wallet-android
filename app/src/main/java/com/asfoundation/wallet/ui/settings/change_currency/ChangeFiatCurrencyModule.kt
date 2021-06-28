package com.asfoundation.wallet.ui.settings.change_currency

import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class ChangeFiatCurrencyModule {

  @Provides
  fun providesChangeFiatCurrencyViewModelFactory(
      fiatCurrenciesService: FiatCurrenciesService): ChangeFiatCurrencyViewModelFactory {
    return ChangeFiatCurrencyViewModelFactory(fiatCurrenciesService, CompositeDisposable())
  }
}