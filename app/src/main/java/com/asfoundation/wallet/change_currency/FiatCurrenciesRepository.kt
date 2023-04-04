package com.asfoundation.wallet.change_currency

import com.appcoins.wallet.core.network.microservices.api.broker.FiatCurrenciesApi
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import com.appcoins.wallet.ui.arch.data.DataResult
import com.appcoins.wallet.ui.arch.data.toDataResult
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: FiatCurrenciesApi,
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource,
  private val fiatCurrenciesMapper: FiatCurrenciesMapper,
  private val fiatCurrenciesDao: FiatCurrenciesDao,
  private val conversionService: LocalCurrencyConversionService,
  private val dispatchers: Dispatchers
) {

  private suspend fun fetchCurrenciesList(): DataResult<List<FiatCurrencyEntity>> =
    withContext(dispatchers.io) {
      fiatCurrenciesApi.getFiatCurrencies()
        .map { response ->
          fiatCurrenciesMapper.mapResponseToCurrencyList(response)
        }
        .onSuccess {
          fiatCurrenciesDao.replaceAllBy(it)
        }
    }


  suspend fun getCurrenciesList(): DataResult<List<FiatCurrencyEntity>> =
    withContext(dispatchers.io) {
      if (fiatCurrenciesPreferencesDataSource.getCurrencyListLastVersion() != BuildConfig.VERSION_CODE) {
        fiatCurrenciesPreferencesDataSource.setCurrencyListLastVersion(BuildConfig.VERSION_CODE)
        return@withContext fetchCurrenciesList()
      } else {
        fiatCurrenciesDao.getFiatCurrencies().toDataResult()
      }
    }

  suspend fun getSelectedCurrency(): DataResult<String> {
    return if (fiatCurrenciesPreferencesDataSource.getSelectCurrency()) {
      val fiatValue: FiatValue = conversionService.localCurrency.await()
      fiatCurrenciesPreferencesDataSource.setSelectedCurrency(fiatValue.currency)
      fiatCurrenciesPreferencesDataSource.setSelectFirstTime()
      fiatValue.currency.toDataResult()
    } else {
      getCachedSelectedCurrency()
    }
  }

  fun getCachedSelectedCurrency(): DataResult<String> {
    return fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency().toDataResult()
  }

  fun setSelectedCurrency(currency: String) {
    fiatCurrenciesPreferencesDataSource.setSelectedCurrency(currency)
  }
}