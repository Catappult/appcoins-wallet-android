package com.asfoundation.wallet.change_currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.CompositeDisposable

class ChangeFiatCurrencyViewModelFactory(private val disposables: CompositeDisposable,
                                         private val selectedCurrencyInteract: SelectedCurrencyInteract) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChangeFiatCurrencyViewModel(disposables,
        selectedCurrencyInteract) as T
  }
}