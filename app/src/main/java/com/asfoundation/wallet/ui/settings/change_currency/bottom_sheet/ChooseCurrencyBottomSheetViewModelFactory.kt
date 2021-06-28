package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ChooseCurrencyBottomSheetViewModelFactory(private val view: ChooseCurrencyBottomSheetView,
                                                private val data: ChooseCurrencyBottomSheetData,
                                                private val disposable: CompositeDisposable,
                                                private val viewScheduler: Scheduler) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChooseCurrencyBottomSheetViewModel(view, data, disposable, viewScheduler) as T
  }
}