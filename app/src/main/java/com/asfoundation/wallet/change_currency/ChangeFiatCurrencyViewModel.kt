package com.asfoundation.wallet.change_currency

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object ChangeFiatCurrencySideEffect : SideEffect

data class ChangeFiatCurrencyState(
    val changeFiatCurrencyAsync: Async<ChangeFiatCurrency> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class ChangeFiatCurrencyViewModel @Inject constructor(
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