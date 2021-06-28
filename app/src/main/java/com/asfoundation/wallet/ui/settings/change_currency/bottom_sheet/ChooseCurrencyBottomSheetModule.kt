package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class ChooseCurrencyBottomSheetModule {

  @Provides
  fun providesChooseCurrencyBottomSheetViewModelFactory(fragment: ChooseCurrencyBottomSheetFragment,
                                                        data: ChooseCurrencyBottomSheetData): ChooseCurrencyBottomSheetViewModelFactory {
    return ChooseCurrencyBottomSheetViewModelFactory(fragment as ChooseCurrencyBottomSheetView,
        data,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesChooseCurrencyBottomSheetData(
      fragment: ChooseCurrencyBottomSheetFragment): ChooseCurrencyBottomSheetData {
    fragment.arguments!!.apply {
      return ChooseCurrencyBottomSheetData(
          getString(ChooseCurrencyBottomSheetFragment.FLAG) as String,
          getString(ChooseCurrencyBottomSheetFragment.CURRENCY) as String,
          getString(ChooseCurrencyBottomSheetFragment.LABEL) as String)
    }
  }
}