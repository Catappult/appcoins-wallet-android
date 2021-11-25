package com.asfoundation.wallet.change_currency

import android.content.SharedPreferences
import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET

class FiatCurrenciesRepository(private val fiatCurrenciesApi: FiatCurrenciesApi,
                               private val pref: SharedPreferences,
                               private val fiatCurrenciesMapper: FiatCurrenciesMapper,
                               private val fiatCurrenciesDao: FiatCurrenciesDao,
                               private val conversionService: LocalCurrencyConversionService) {

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    private const val SELECTED_FIRST_TIME = "selected_first_time"
    private const val CURRENCY_LIST_FIRST_TIME = "currency_list_first_time"
  }

  private fun fetchCurrenciesList(): Single<List<FiatCurrencyEntity>> {
    return fiatCurrenciesApi.getFiatCurrencies()
        .map { response: FiatCurrenciesResponse ->
          fiatCurrenciesMapper.mapResponseToCurrencyList(response)
        }
        .flatMap {
          return@flatMap Completable.fromAction {
            fiatCurrenciesDao.replaceAllBy(it)
          }
              .toSingle { it }
        }
        .subscribeOn(Schedulers.io())
  }

  fun getCurrenciesList(): Single<List<FiatCurrencyEntity>> {
    return Single.just(pref.getBoolean(CURRENCY_LIST_FIRST_TIME, true))
        .flatMap { firstTime ->
          if (firstTime) {
            pref.edit()
                .putBoolean(CURRENCY_LIST_FIRST_TIME, false)
                .apply()
            fetchCurrenciesList()
          } else {
            fiatCurrenciesDao.getFiatCurrencies()
          }
        }
        .subscribeOn(Schedulers.io())

  }

  fun getSelectedCurrency(): Single<String> {
    return Single.just(pref.getBoolean(SELECTED_FIRST_TIME, true))
        .flatMapCompletable { isFirstTime ->
          if (isFirstTime) {
            pref.edit()
                .putBoolean(SELECTED_FIRST_TIME, false)
                .apply()
            return@flatMapCompletable conversionService.localCurrency.doOnSuccess {
              setSelectedCurrency(it.currency)
            }
                .ignoreElement()
          }
          return@flatMapCompletable Completable.complete()
        }
        .andThen(getCachedSelectedCurrency())
        .subscribeOn(Schedulers.io())
  }

  fun getCachedSelectedCurrency(): Single<String> {
    return Single.just(pref.getString(FIAT_CURRENCY, "")!!)
        .subscribeOn(Schedulers.io())
  }

  fun setSelectedCurrency(currency: String) {
    pref.edit()
        .putString(FIAT_CURRENCY, currency)
        .apply()
  }

  interface FiatCurrenciesApi {
    @GET("broker/8.20210201/currencies?type=FIAT&icon.height=128")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}