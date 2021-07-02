package com.asfoundation.wallet.service.currencies

import android.util.Log
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET

class FiatCurrenciesService(
    private val fiatCurrenciesApi: FiatCurrenciesApi) {

  fun getApiToFiatCurrency(): Observable<MutableList<FiatCurrency>> {
    return fiatCurrenciesApi.getFiatCurrencies()
        .map { response: FiatCurrenciesResponse ->
          val currencyList: MutableList<FiatCurrency> = ArrayList()
          for (currencyItem in response.items) {
            currencyList.add(
                FiatCurrency(currencyItem.flag, currencyItem.currency, currencyItem.label,
                    currencyItem.sign))
          }
          currencyList
        }
        .subscribeOn(Schedulers.io())
        .doOnError {
          Log.d("APPC-2472",
              "getApiToFiatCurrency: error : ${it.message}")
        }
  }

  interface FiatCurrenciesApi {
    @GET("broker/8.20210201/currencies?type=FIAT")
    fun getFiatCurrencies(): Observable<FiatCurrenciesResponse>
  }

  companion object {
    const val CONVERSION_HOST = BuildConfig.BASE_HOST
  }
}