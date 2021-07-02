package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.settings.change_currency.SelectedCurrencyInteract
import io.reactivex.Scheduler

class ChooseCurrencyBottomSheetViewModelFactory(private val view: ChooseCurrencyBottomSheetView,
                                                private val data: ChooseCurrencyBottomSheetData,
                                                private val viewScheduler: Scheduler,
                                                private val selectedCurrencyInteract: SelectedCurrencyInteract,
                                                private val navigator: ChooseCurrencyBottomSheetNavigator) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChooseCurrencyBottomSheetViewModel(view, data, viewScheduler, selectedCurrencyInteract,
        navigator) as T
  }
}