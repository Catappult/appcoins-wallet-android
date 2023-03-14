package com.asfoundation.wallet.change_currency

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import retrofit2.http.GET
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: FiatCurrenciesApi,
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource,
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
    return Single.just(fiatCurrenciesPreferencesDataSource.getCurrencyListLastVersion())
      .flatMap { lastVersion ->
        if (lastVersion != BuildConfig.VERSION_CODE) {
          fiatCurrenciesPreferencesDataSource.setCurrencyListLastVersion(BuildConfig.VERSION_CODE)
          fetchCurrenciesList()
        } else {
          fiatCurrenciesDao.getFiatCurrencies()
        }
      }
      .subscribeOn(Schedulers.io())

  }

  fun getSelectedCurrency(): Single<String> {
    return Single.just(fiatCurrenciesPreferencesDataSource.getSelectCurrency())
      .flatMap { isFirstTime ->
        if (isFirstTime) {
          return@flatMap conversionService.localCurrency.doOnSuccess {
            fiatCurrenciesPreferencesDataSource.setSelectedCurrency(it.currency)
            fiatCurrenciesPreferencesDataSource.setSelectFirstTime()
          }.map { it.currency }
        }
        return@flatMap getCachedSelectedCurrency()
      }
      .subscribeOn(Schedulers.io())
  }

  fun getCachedSelectedCurrency(): Single<String> {
    return Single.just(fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency())
      .subscribeOn(Schedulers.io())
  }

  fun setSelectedCurrency(currency: String) {
    fiatCurrenciesPreferencesDataSource.setSelectedCurrency(currency)
  }

  interface FiatCurrenciesApi {
    @GET("8.20210201/currencies?type=FIAT&icon.height=128")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }

}