package com.asfoundation.wallet.change_currency.bottom_sheet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


sealed class ChooseCurrencyBottomSideEffect : SideEffect {
  object ShowConfirmationLoading : ChooseCurrencyBottomSideEffect()
  object NavigateBack : ChooseCurrencyBottomSideEffect()
}

data class ChooseCurrencyBottomSheetState(val selectedCurrency: String,
                                          val selectedFlag: String,
                                          val selectedLabel: String,
                                          val selectedConfirmationAsync: Async<Unit> = Async.Uninitialized) :
    ViewState

class NewChooseCurrencyBottomSheetViewModel(data: ChooseCurrencyBottomSheetData,
                                            private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase) :
    BaseViewModel<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect>(
        initialState(data)) {

  companion object {
    fun initialState(data: ChooseCurrencyBottomSheetData): ChooseCurrencyBottomSheetState {
      return ChooseCurrencyBottomSheetState(data.currency!!, data.flag!!, data.label!!)
    }
  }

  fun currencyConfirmationClick() {
    Single.just(setSelectedCurrencyUseCase(state.selectedCurrency))
        .subscribeOn(Schedulers.io())
        .asAsyncToState() {
          copy(selectedConfirmationAsync = it)
        }
  }
}