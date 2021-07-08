package com.asfoundation.wallet.ui.settings.change_currency

import android.content.SharedPreferences
import android.util.Log
import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class SelectedCurrencyInteract(private val pref: SharedPreferences,
                               private val balanceInteractor: BalanceInteractor,
                               private val fiatCurrenciesService: FiatCurrenciesService) {

  lateinit var fiatCurrency: FiatCurrency

  private companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
  }

  fun setSelectedCurrency(fiatCurrency: FiatCurrency) {
    Log.d("APPC-2472", "SelectedCurrencyInteract: setSelectedCurrency: $fiatCurrency")
    pref.edit()
        .putString(FIAT_CURRENCY, Gson().toJson(fiatCurrency))
        .apply()
  }

  fun getSelectedCurrency(): FiatCurrency {

    return if (selectedCurrencyPrefExists()) {
      Gson().fromJson(pref.getString(FIAT_CURRENCY, ""), FiatCurrency::class.java)
    } else {
      mapLocalCurrency()
//      FiatCurrency("test", "test", "test", "test")
    }
  }

  private fun selectedCurrencyPrefExists(): Boolean {
    return pref.contains(FIAT_CURRENCY)
  }

  fun getApiToFiatCurrency(): Observable<MutableList<FiatCurrency>> {
    return fiatCurrenciesService.getApiToFiatCurrency()
  }

  private fun getList(): MutableList<FiatCurrency> {
    return getApiToFiatCurrency().blockingFirst()
  }

  private fun getPair(): Pair<Balance, FiatValue> {
    return balanceInteractor.getAppcBalance().blockingFirst()
  }

  private fun mapLocalCurrency(): FiatCurrency {

    for (fiat in getList()) {
      if (fiat.currency == getPair().second.currency) {
        Log.d("APPC-2472",
            "SelectedCurrencyInteract: mapLocalCurrency: is local currency $fiat ")
        fiatCurrency = fiat
        break
      }
    }
    return fiatCurrency
  }

//  private fun getLocalCurrency(): Disposable? {
//    return Observable.zip(getApiToFiatCurrency(), balanceInteractor.getAppcBalance(),
//        { currencyList, tokenBalance ->
//          mapLocalCurrency1(currencyList, tokenBalance)
//        })
//        .subscribe()
//  }
//
//  private fun mapLocalCurrency1(currencyList: MutableList<FiatCurrency>,
//                               tokenBalance: Pair<Balance, FiatValue>): FiatCurrency {
//    for (fiat in currencyList) {
//      if (fiat.currency == tokenBalance.second.currency) {
//        Log.d("APPC-2472",
//            "SelectedCurrencyInteract: mapLocalCurrency: is local currency $fiat ")
//        fiatCurrency = fiat
//      }
//    }
//    return fiatCurrency
//  }
//

}