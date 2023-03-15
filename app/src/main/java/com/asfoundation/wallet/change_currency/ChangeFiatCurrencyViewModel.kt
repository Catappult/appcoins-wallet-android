package com.asfoundation.wallet.change_currency

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object ChangeFiatCurrencySideEffect : com.appcoins.wallet.ui.arch.SideEffect

data class ChangeFiatCurrencyState(
    val changeFiatCurrencyAsync: com.appcoins.wallet.ui.arch.Async<ChangeFiatCurrency> = com.appcoins.wallet.ui.arch.Async.Uninitialized) :
  com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class ChangeFiatCurrencyViewModel @Inject constructor(
    private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase) :
    com.appcoins.wallet.ui.arch.BaseViewModel<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect>(
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