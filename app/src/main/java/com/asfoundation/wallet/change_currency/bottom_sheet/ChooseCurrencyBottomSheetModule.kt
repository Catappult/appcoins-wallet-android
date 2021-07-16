package com.asfoundation.wallet.change_currency.bottom_sheet

import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers

@Module
class ChooseCurrencyBottomSheetModule {

  @Provides
  fun providesChooseCurrencyBottomSheetViewModelFactory(data: ChooseCurrencyBottomSheetData,
                                                        setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase): ChooseCurrencyBottomSheetViewModelFactory {
    return ChooseCurrencyBottomSheetViewModelFactory(data, Schedulers.io(),
        setSelectedCurrencyUseCase)
  }

  @Provides
  fun providesChooseCurrencyBottomSheetData(
      fragment: ChooseCurrencyBottomSheetFragment): ChooseCurrencyBottomSheetData {
    fragment.requireArguments()
        .apply {
          return ChooseCurrencyBottomSheetData(
              getString(ChooseCurrencyBottomSheetFragment.FLAG) as String,
              getString(ChooseCurrencyBottomSheetFragment.CURRENCY) as String,
              getString(ChooseCurrencyBottomSheetFragment.LABEL) as String,
              getString(ChooseCurrencyBottomSheetFragment.SIGN) as String)
        }
  }

  @Provides
  fun providesChooseCurrencyBottomSheetNavigator(
      fragment: ChooseCurrencyBottomSheetFragment): ChooseCurrencyBottomSheetNavigator {
    return ChooseCurrencyBottomSheetNavigator(fragment)
  }
}