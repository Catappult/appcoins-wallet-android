package com.asfoundation.wallet.service.currencies

import android.content.SharedPreferences
import android.util.Log
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET

class FiatCurrenciesService(
    private val fiatCurrenciesApi: FiatCurrenciesApi,
    private val pref: SharedPreferences) {

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    const val CONVERSION_HOST = BuildConfig.BASE_HOST
  }

  fun getApiToFiatCurrency(): Single<List<FiatCurrency>> {
    return fiatCurrenciesApi.getFiatCurrencies()
        .map { response: FiatCurrenciesResponse ->
          val currencyList: MutableList<FiatCurrency> = ArrayList()
          for (currencyItem in response.items) {
            currencyList.add(
                FiatCurrency(currencyItem.flag, currencyItem.currency, currencyItem.label,
                    currencyItem.sign))
          }
          currencyList.toList()
        }
        .subscribeOn(Schedulers.io())
        .doOnError {
          Log.d("APPC-2472",
              "getApiToFiatCurrency: error : ${it.message}")
        }
  }

  fun getSelectedCurrency(): Single<String> {
    return Single.just(pref.getString(FIAT_CURRENCY, ""))
  }

  fun setSelectedCurrency() {

  }

  interface FiatCurrenciesApi {
    @GET("broker/8.20210201/currencies?type=FIAT")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}