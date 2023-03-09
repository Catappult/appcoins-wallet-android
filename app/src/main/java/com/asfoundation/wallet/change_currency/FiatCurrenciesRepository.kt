package com.asfoundation.wallet.change_currency

import android.content.SharedPreferences
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(private val fiatCurrenciesApi: FiatCurrenciesApi,
                                                   private val pref: SharedPreferences,
                                                   private val fiatCurrenciesMapper: FiatCurrenciesMapper,
                                                   private val fiatCurrenciesDao: FiatCurrenciesDao,
                                                   private val conversionService: LocalCurrencyConversionService) {

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    private const val SELECTED_FIRST_TIME = "selected_first_time"
    private const val CURRENCY_LIST_LAST_VERSION = "currency_list_last_version"
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
    return Single.just(pref.getInt(CURRENCY_LIST_LAST_VERSION, 0))
        .flatMap { lastVersion ->
          if (lastVersion != BuildConfig.VERSION_CODE) {
            pref.edit()
                .putInt(CURRENCY_LIST_LAST_VERSION, BuildConfig.VERSION_CODE)
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
        .flatMap { isFirstTime ->
          if (isFirstTime) {
            return@flatMap conversionService.localCurrency.doOnSuccess {
              setSelectedCurrency(it.currency)
              pref.edit()
                  .putBoolean(SELECTED_FIRST_TIME, false)
                  .apply()
            }
                .map { it.currency }
          }
          return@flatMap getCachedSelectedCurrency()
        }
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
    @GET("8.20210201/currencies?type=FIAT&icon.height=128")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}