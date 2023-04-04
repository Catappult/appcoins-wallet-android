package com.asfoundation.wallet.change_currency

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.appcoins.wallet.ui.arch.data.Async
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

object ChangeFiatCurrencySideEffect : SideEffect

data class ChangeFiatCurrencyState(
  val changeFiatCurrencyAsync: Async<ChangeFiatCurrency> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChangeFiatCurrencyViewModel @Inject constructor(
  private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase
) : BaseViewModel<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect>(initialState()) {

  companion object {
    fun initialState(): ChangeFiatCurrencyState {
      return ChangeFiatCurrencyState()
    }
  }

  init {
    showChangeFiatCurrency()
  }

  private fun showChangeFiatCurrency() {
    viewModelScope.launch {
      suspend { getChangeFiatCurrencyModelUseCase() }
        .mapResultAsyncToState {
          copy(changeFiatCurrencyAsync = it)
        }
    }
  }
}