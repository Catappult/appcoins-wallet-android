package com.asfoundation.wallet.ui.settings.change_currency

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class ChangeFiatCurrencyModule {

  @Provides
  fun providesChangeFiatCurrencyViewModelFactory(
      selectedCurrencyInteract: SelectedCurrencyInteract): ChangeFiatCurrencyViewModelFactory {
    return ChangeFiatCurrencyViewModelFactory(CompositeDisposable(),
        selectedCurrencyInteract)
  }
}