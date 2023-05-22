package com.asfoundation.wallet.change_currency.bottom_sheet

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
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
      fragment: Fragment): ChooseCurrencyBottomSheetData {
    fragment.requireArguments()
        .apply {
          return ChooseCurrencyBottomSheetData(
              getString(ChooseCurrencyBottomSheetFragment.FLAG),
              getString(ChooseCurrencyBottomSheetFragment.CURRENCY) as String,
              getString(ChooseCurrencyBottomSheetFragment.LABEL) as String,
              getString(ChooseCurrencyBottomSheetFragment.SIGN) as String)
        }
  }
}