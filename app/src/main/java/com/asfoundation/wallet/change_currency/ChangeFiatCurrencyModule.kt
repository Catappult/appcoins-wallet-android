package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.Module
import dagger.Provides

@Module
class ChangeFiatCurrencyModule {

  @Provides
  fun providesChangeFiatCurrencyViewModelFactory(
      getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase): ChangeFiatCurrencyViewModelFactory {
    return ChangeFiatCurrencyViewModelFactory(getChangeFiatCurrencyModelUseCase)
  }

  @Provides
  fun providesChangeFiatCurrencyNavigator(
      fragment: ChangeFiatCurrencyFragment): ChangeFiatCurrencyNavigator {
    return ChangeFiatCurrencyNavigator(fragment)
  }
}