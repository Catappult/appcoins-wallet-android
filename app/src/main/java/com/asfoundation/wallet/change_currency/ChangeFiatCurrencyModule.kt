package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import dagger.Module
import dagger.Provides

@Module
class ChangeFiatCurrencyModule {

  @Provides
  fun providesChangeFiatCurrencyViewModelFactory(
      getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase): ChangeFiatCurrencyViewModelFactory {
    return ChangeFiatCurrencyViewModelFactory(getSelectedCurrencyUseCase)
  }
}