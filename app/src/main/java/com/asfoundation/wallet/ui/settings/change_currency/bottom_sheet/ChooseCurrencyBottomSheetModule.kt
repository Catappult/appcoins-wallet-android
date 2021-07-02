package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import com.asfoundation.wallet.ui.settings.change_currency.SelectedCurrencyInteract
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
class ChooseCurrencyBottomSheetModule {

  @Provides
  fun providesChooseCurrencyBottomSheetViewModelFactory(fragment: ChooseCurrencyBottomSheetFragment,
                                                        data: ChooseCurrencyBottomSheetData,
                                                        selectedCurrencyInteract: SelectedCurrencyInteract,
                                                        navigator: ChooseCurrencyBottomSheetNavigator): ChooseCurrencyBottomSheetViewModelFactory {
    return ChooseCurrencyBottomSheetViewModelFactory(fragment as ChooseCurrencyBottomSheetView,
        data, AndroidSchedulers.mainThread(), selectedCurrencyInteract, navigator)
  }

  @Provides
  fun providesChooseCurrencyBottomSheetData(
      fragment: ChooseCurrencyBottomSheetFragment): ChooseCurrencyBottomSheetData {
    fragment.arguments!!.apply {
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
    return ChooseCurrencyBottomSheetNavigator(fragment.requireFragmentManager())
  }
}