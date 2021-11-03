package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase

object ChangeFiatCurrencySideEffect : SideEffect

data class ChangeFiatCurrencyState(
    val changeFiatCurrencyAsync: Async<ChangeFiatCurrency> = Async.Uninitialized) :
    ViewState

class ChangeFiatCurrencyViewModel(
    private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase) :
    BaseViewModel<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect>(
        initialState()) {

  companion object {
    fun initialState(): ChangeFiatCurrencyState {
      return ChangeFiatCurrencyState()
    }
  }

  init {
    showChangeFiatCurrency()
  }

  private fun showChangeFiatCurrency() {
    getChangeFiatCurrencyModelUseCase()
        .asAsyncToState(ChangeFiatCurrencyState::changeFiatCurrencyAsync) {
          copy(changeFiatCurrencyAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }
}