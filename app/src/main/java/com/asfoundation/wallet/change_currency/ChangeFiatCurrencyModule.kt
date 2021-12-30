package com.asfoundation.wallet.change_currency

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class ChangeFiatCurrencyModule {

  @Provides
  fun providesChangeFiatCurrencyViewModelFactory(
      getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase): ChangeFiatCurrencyViewModelFactory {
    return ChangeFiatCurrencyViewModelFactory(getChangeFiatCurrencyModelUseCase)
  }

  @Provides
  fun providesChangeFiatCurrencyNavigator(fragment: Fragment): ChangeFiatCurrencyNavigator {
    return ChangeFiatCurrencyNavigator(fragment)
  }
}