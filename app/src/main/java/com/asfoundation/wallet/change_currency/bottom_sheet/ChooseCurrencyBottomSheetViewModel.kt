package com.asfoundation.wallet.change_currency.bottom_sheet

import android.util.Log
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler


sealed class ChooseCurrencyBottomSideEffect : SideEffect {
  object NavigateBack : ChooseCurrencyBottomSideEffect()
}

data class ChooseCurrencyBottomSheetState(val selectedCurrency: String,
                                          val selectedFlag: String,
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
      return ChooseCurrencyBottomSheetState(data.currency!!, data.flag!!, data.label!!)
    }
  }

  fun currencyConfirmationClick() {
    Observable.just(setSelectedCurrencyUseCase(state.selectedCurrency))
        .subscribeOn(networkScheduler)
        .asAsyncToState() {
          Log.d("APPC-2472",
              "showChangeFiatCurrency: currencyConfirmationClick ${state.selectedConfirmationAsync} ... $it")
          copy(selectedConfirmationAsync = it)
        }
  }
}