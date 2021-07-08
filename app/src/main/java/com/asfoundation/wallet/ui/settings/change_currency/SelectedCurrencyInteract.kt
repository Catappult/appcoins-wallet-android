package com.asfoundation.wallet.ui.settings.change_currency

import android.content.SharedPreferences
import android.util.Log
import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.service.currencies.FiatCurrenciesService
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.gson.Gson
import io.reactivex.Single

// Rename
class SelectedCurrencyInteract(private val pref: SharedPreferences,
                               private val fiatCurrenciesService: FiatCurrenciesService,
                               private val conversionService: LocalCurrencyConversionService) {

  lateinit var fiatCurrency: FiatCurrency

  private companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
  }

  fun getChangeFiatCurrencyModel(): Single<ChangeFiatCurrency> {
    return Single.zip(fiatCurrenciesService.getApiToFiatCurrency(),
        fiatCurrenciesService.getSelectedCurrency(),
        { list, selectedCurrency -> ChangeFiatCurrency(list, selectedCurrency) })
        .flatMap { changeFiatCurrencyModel ->
          if (changeFiatCurrencyModel.selectedCurrency.isEmpty()) {
            return@flatMap conversionService.localCurrency
                .map { localCurrency ->
                  changeFiatCurrencyModel.copy(selectedCurrency = localCurrency.currency)
                }
          }
          return@flatMap Single.just(changeFiatCurrencyModel)
        }
  }

  private fun mapToChangeFiatCurrencyModel(list: List<FiatCurrency>,
                                           selectedCurrency: String): ChangeFiatCurrency {
    if (selectedCurrency.isEmpty()) {
      // Buscar getPair()
    }
    return ChangeFiatCurrency(list, selectedCurrency)
  }

  fun setSelectedCurrency(fiatCurrency: FiatCurrency) {
    Log.d("APPC-2472", "SelectedCurrencyInteract: setSelectedCurrency: $fiatCurrency")
    pref.edit()
        .putString(FIAT_CURRENCY, Gson().toJson(fiatCurrency))
        .apply()
  }


  private fun selectedCurrencyPrefExists(): Boolean {
    return pref.contains(FIAT_CURRENCY)
  }

  private fun getList(): MutableList<FiatCurrency> {
    return getApiToFiatCurrency().blockingFirst()
  }

  private fun getPair(): Pair<Balance, FiatValue> {
    return balanceInteractor.getAppcBalance()
        .blockingFirst()
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