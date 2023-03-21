package com.asfoundation.wallet.change_currency.bottom_sheet

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import io.reactivex.Scheduler


sealed class ChooseCurrencyBottomSideEffect : SideEffect {
  object NavigateBack : ChooseCurrencyBottomSideEffect()
}

data class ChooseCurrencyBottomSheetState(val selectedCurrency: String,
                                          val selectedFlag: String?,
                                          val selectedLabel: String,
                                          val selectedConfirmationAsync: Async<Unit> = Async.Uninitialized) :
  ViewState

class ChooseCurrencyBottomSheetViewModel(data: ChooseCurrencyBottomSheetData,
                                         private val networkScheduler: Scheduler,
                                         private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase) :
    BaseViewModel<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect>(
        initialState(data)) {

  companion object {
    fun initialState(data: ChooseCurrencyBottomSheetData): ChooseCurrencyBottomSheetState {
      return ChooseCurrencyBottomSheetState(data.currency, data.flag, data.label)
    }
  }

  fun currencyConfirmationClick() {
    setSelectedCurrencyUseCase(state.selectedCurrency)
        .subscribeOn(networkScheduler)
        .asAsyncToState() {
          copy(selectedConfirmationAsync = it)
        }
        .scopedSubscribe { e ->
          e.printStackTrace()
        }
  }
}