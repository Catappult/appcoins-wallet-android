package com.asfoundation.wallet.ui.settings.change_currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import io.reactivex.disposables.CompositeDisposable

class ChangeFiatCurrencyViewModelFactory(private val fiatCurrenciesService: FiatCurrenciesService,
                                         private val disposables: CompositeDisposable) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ChangeFiatCurrencyViewModel(fiatCurrenciesService, disposables) as T
  }
}