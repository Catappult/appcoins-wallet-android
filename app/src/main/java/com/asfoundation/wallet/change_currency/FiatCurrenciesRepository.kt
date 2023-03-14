package com.asfoundation.wallet.change_currency

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import repository.FiatCurrenciesSharedPreferences
import retrofit2.http.GET
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: FiatCurrenciesApi,
  private val fiatCurrenciesSharedPreferences: FiatCurrenciesSharedPreferences,
  private val fiatCurrenciesMapper: FiatCurrenciesMapper,
  private val fiatCurrenciesDao: FiatCurrenciesDao,
  private val conversionService: LocalCurrencyConversionService
) {

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
    return Single.just(fiatCurrenciesSharedPreferences.getCurrencyListLastVersion())
      .flatMap { lastVersion ->
        if (lastVersion != BuildConfig.VERSION_CODE) {
          fiatCurrenciesSharedPreferences.setCurrencyListLastVersion(BuildConfig.VERSION_CODE)
          fetchCurrenciesList()
        } else {
          fiatCurrenciesDao.getFiatCurrencies()
        }
      }
      .subscribeOn(Schedulers.io())

  }

  fun getSelectedCurrency(): Single<String> {
    return Single.just(fiatCurrenciesSharedPreferences.getSelectCurrency())
      .flatMap { isFirstTime ->
        if (isFirstTime) {
          return@flatMap conversionService.localCurrency.doOnSuccess {
            fiatCurrenciesSharedPreferences.setSelectedCurrency(it.currency)
            fiatCurrenciesSharedPreferences.setSelectFirstTime()
          }.map { it.currency }
        }
        return@flatMap getCachedSelectedCurrency()
      }
      .subscribeOn(Schedulers.io())
  }

  fun getCachedSelectedCurrency(): Single<String> {
    return Single.just(fiatCurrenciesSharedPreferences.getCachedSelectedCurrency())
      .subscribeOn(Schedulers.io())
  }

  fun setSelectedCurrency(currency: String) {
    fiatCurrenciesSharedPreferences.setSelectedCurrency(currency)
  }

  interface FiatCurrenciesApi {
    @GET("8.20210201/currencies?type=FIAT&icon.height=128")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}