package com.appcoins.wallet.feature.changecurrency.ui

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.appcoins.wallet.ui.arch.data.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

object ChangeFiatCurrencySideEffect : SideEffect

data class ChangeFiatCurrencyState(
  val changeFiatCurrencyAsync: Async<com.appcoins.wallet.feature.changecurrency.data.ChangeFiatCurrency> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChangeFiatCurrencyViewModel @Inject constructor(
  private val getChangeFiatCurrencyModelUseCase: com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase,
) : BaseViewModel<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect>(ChangeFiatCurrencyState()) {

  init {
    showChangeFiatCurrency()
  }

  private fun showChangeFiatCurrency() {
    viewModelScope.launch {
      suspend { getChangeFiatCurrencyModelUseCase() }
        .mapResultAsyncToState() {
          copy(changeFiatCurrencyAsync = it)
        }
    }
  }
}