package com.appcoins.wallet.feature.changecurrency.ui

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.changecurrency.data.ChangeFiatCurrency
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

object ChangeFiatCurrencySideEffect : SideEffect

data class ChangeFiatCurrencyState(
  val changeFiatCurrencyAsync: Async<ChangeFiatCurrency> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChangeFiatCurrencyViewModel @Inject constructor(
  private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
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