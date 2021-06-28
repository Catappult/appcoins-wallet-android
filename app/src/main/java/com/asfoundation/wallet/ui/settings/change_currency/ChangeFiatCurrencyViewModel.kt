package com.asfoundation.wallet.ui.settings.change_currency

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import com.asfoundation.wallet.viewmodel.BaseViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangeFiatCurrencyViewModel(private val fiatCurrenciesService: FiatCurrenciesService,
                                  private val disposables: CompositeDisposable) :
    BaseViewModel() {

  private val currencyList: MutableLiveData<MutableList<FiatCurrency>> = MutableLiveData()

  init {
    showCurrencyList()
  }

  override fun onCleared() {
    disposables.clear()
    super.onCleared()
  }

  fun currencyList(): MutableLiveData<MutableList<FiatCurrency>> {
    return currencyList
  }

  fun showCurrencyList() {
    disposables.add(fiatCurrenciesService.getApiToFiatCurrency()
        .doOnNext() {
          Log.d("APPC-2472", "showCurrencyList: size of the list -> ${it.size}")
          currencyList.postValue(it)
        }
        .doOnError {
          Log.d("APPC-2472", "showCurrencyList: error ${it.message}")
        }
        .subscribe())
  }
}