package com.asfoundation.wallet.ui.settings.change_currency

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import com.asfoundation.wallet.viewmodel.BaseViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangeFiatCurrencyViewModel(private val fiatCurrenciesService: FiatCurrenciesService,
                                  private val disposables: CompositeDisposable,
                                  private val selectedCurrencyInteract: SelectedCurrencyInteract) :
    BaseViewModel() {

  private val currencyList: MutableLiveData<MutableList<FiatCurrency>> = MutableLiveData()
  private val selectedCurrency: MutableLiveData<FiatCurrency> = MutableLiveData()

  init {
    showCurrencyList()
    showSelectedCurrency()
  }

  override fun onCleared() {
    disposables.clear()
    super.onCleared()
  }

  fun currencyList(): MutableLiveData<MutableList<FiatCurrency>> {
    return currencyList
  }

  fun selectedCurrency(): MutableLiveData<FiatCurrency> {
    return selectedCurrency
  }

  fun showCurrencyList() {
    disposables.add(fiatCurrenciesService.getApiToFiatCurrency()
        .doOnNext() {
          currencyList.postValue(it)
        }
        .doOnError {
          Log.d("APPC-2472", "showCurrencyList: error ${it.message}")
        }
        .subscribe())
  }

  fun showSelectedCurrency() {
    selectedCurrency.postValue(selectedCurrencyInteract.getSelectedCurrency())
  }
}