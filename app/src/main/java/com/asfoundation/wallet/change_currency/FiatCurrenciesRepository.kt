package com.asfoundation.wallet.change_currency

import com.appcoins.wallet.core.network.microservices.ProductApiModule
import com.appcoins.wallet.core.network.microservices.model.FiatCurrenciesResponse
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: ProductApiModule.FiatCurrenciesApi,
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
}