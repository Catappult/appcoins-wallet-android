package com.asfoundation.wallet.change_currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase

class ChangeFiatCurrencyViewModelFactory(
    private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChangeFiatCurrencyViewModel(getChangeFiatCurrencyModelUseCase) as T
  }
}