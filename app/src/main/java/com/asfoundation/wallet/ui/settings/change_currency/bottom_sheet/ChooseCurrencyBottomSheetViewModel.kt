package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import com.asfoundation.wallet.viewmodel.BaseViewModel
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ChooseCurrencyBottomSheetViewModel(private val view: ChooseCurrencyBottomSheetView,
                                         private val data: ChooseCurrencyBottomSheetData,
                                         private val disposable: CompositeDisposable,
                                         private val viewScheduler: Scheduler) : BaseViewModel() {

  init {
    data.flag?.let { view.setCurrencyFlag(it) }
    data.currency?.let { view.setCurrencyShort(it) }
    data.label?.let { view.setCurrencyLabel(it) }
  }
}