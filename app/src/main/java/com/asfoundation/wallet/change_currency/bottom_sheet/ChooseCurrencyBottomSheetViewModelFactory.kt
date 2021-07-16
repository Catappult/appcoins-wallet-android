package com.asfoundation.wallet.change_currency.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import io.reactivex.Scheduler

class ChooseCurrencyBottomSheetViewModelFactory(private val data: ChooseCurrencyBottomSheetData,
                                                private val networkScheduler: Scheduler,
                                                private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChooseCurrencyBottomSheetViewModel(data, networkScheduler,
        setSelectedCurrencyUseCase) as T
  }
}