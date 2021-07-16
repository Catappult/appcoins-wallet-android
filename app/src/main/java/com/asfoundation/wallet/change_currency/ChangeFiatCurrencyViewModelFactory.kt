package com.asfoundation.wallet.change_currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase

class ChangeFiatCurrencyViewModelFactory(
    private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return NewChangeFiatCurrencyViewModel(getSelectedCurrencyUseCase) as T
  }
}