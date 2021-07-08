package com.asfoundation.wallet.ui.settings.change_currency

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.asfoundation.wallet.viewmodel.BaseViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangeFiatCurrencyViewModel(private val disposables: CompositeDisposable,
                                  private val selectedCurrencyInteract: SelectedCurrencyInteract) :
    BaseViewModel() {

  private val _changeFiatCurrencyLiveData: MutableLiveData<ChangeFiatCurrency> = MutableLiveData()
  val changeFiatCurrencyLiveData: LiveData<ChangeFiatCurrency> = _changeFiatCurrencyLiveData

  init {
    showCurrencyList()
  }

  override fun onCleared() {
    disposables.clear()
    super.onCleared()
  }

  fun showCurrencyList() {
    disposables.add(selectedCurrencyInteract.getChangeFiatCurrencyModel()
        .doOnSuccess { _changeFiatCurrencyLiveData.postValue(it) }
        .doOnError {
          Log.d("APPC-2472", "showCurrencyList: error ${it.message}")
        }
        .subscribe())
  }

}