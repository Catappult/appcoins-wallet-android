package com.asfoundation.wallet.ui.settings.change_currency

import android.content.SharedPreferences
import android.util.Log
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET

class FiatCurrenciesRepository(private val fiatCurrenciesApi: FiatCurrenciesApi,
                               private val pref: SharedPreferences,
                               private val fiatCurrenciesMapper: FiatCurrenciesMapper) {

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    const val CONVERSION_HOST = BuildConfig.BASE_HOST
  }

  fun getApiToFiatCurrency(): Single<List<FiatCurrency>> {
    Log.d("APPC-2472", "FiatCurrenciesRepository: getApiToFiatCurrency: begin")
    return fiatCurrenciesApi.getFiatCurrencies()
        .doOnSuccess {
          Log.d("APPC-2472", "FiatCurrenciesRepository: getApiToFiatCurrency: api request success")
        }
        .map { response: FiatCurrenciesResponse ->
          fiatCurrenciesMapper.mapResponseToCurrencyList(response)
        }
        .doOnSuccess {
          Log.d("APPC-2472", "FiatCurrenciesRepository: getApiToFiatCurrency: onSuccess")
        }
        .subscribeOn(Schedulers.io())
        .doOnError {
          Log.d("APPC-2472",
              "getApiToFiatCurrency: error : ${it.message}")
        }
  }

  fun getSelectedCurrency(): Single<String> {
    Log.d("APPC-2472", "FiatCurrenciesRepository: getSelectedCurrency: ${
      pref.getString(
          FIAT_CURRENCY, "")
    }")
    return Single.just(pref.getString(FIAT_CURRENCY, ""))
  }

  fun setSelectedCurrency(currency: String) {
    Log.d("APPC-2472", "FiatCurrenciesRepository: setSelectedCurrency: $currency")
    pref.edit()
        .putString(FIAT_CURRENCY, currency)
        .apply()
  }

  interface FiatCurrenciesApi {
    @GET("broker/8.20210201/currencies?type=FIAT")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}