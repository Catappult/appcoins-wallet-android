package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import com.asfoundation.wallet.ui.settings.change_currency.SelectedCurrencyInteract
import com.asfoundation.wallet.viewmodel.BaseViewModel
import io.reactivex.Scheduler

class ChooseCurrencyBottomSheetViewModel(private val view: ChooseCurrencyBottomSheetView,
                                         private val data: ChooseCurrencyBottomSheetData,
                                         private val viewScheduler: Scheduler,
                                         private val selectedCurrencyInteract: SelectedCurrencyInteract,
                                         private val navigator: ChooseCurrencyBottomSheetNavigator) :
    BaseViewModel() {

  init {
    data.flag?.let { view.setCurrencyFlag(it) }
    data.currency?.let { view.setCurrencyShort(it) }
    data.label?.let { view.setCurrencyLabel(it) }
    currencyConfirmationClick()
  }

  private fun currencyConfirmationClick() {
    disposable.add(view.getConfirmationClick()
        .observeOn(viewScheduler)
        .doOnNext {
          view.showLoading()
          selectedCurrencyInteract.setSelectedCurrency(
              FiatCurrency(data.flag, data.currency, data.label, data.sign))
        }
//        .delay(5, TimeUnit.SECONDS)
        .doOnNext {
          navigator.navigateBack()
        }
        .subscribe({}, { it.printStackTrace() }))
  }
}